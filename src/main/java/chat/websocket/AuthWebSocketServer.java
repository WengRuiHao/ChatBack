package chat.websocket;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import chat.config.SpringConfigurator;
import chat.customJson.CustomJson;
import chat.customJson.OfflineUser;
import chat.customJson.UserToRoom;
import chat.model.dto.ChatDto;
import chat.model.dto.MessageDto;
import chat.service.ChatService;
import chat.service.MessageService;
import chat.util.JwtUtil;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/home/chat", configurator = SpringConfigurator.class)
@Component
public class AuthWebSocketServer {

	@Autowired
	private RabbitMQServer RabbitMQServer;

	@Autowired
	private ChatService chatService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MessageService messageService;

	// 記錄在線人員
	private static final ConcurrentHashMap<String, Session> userSession = new ConcurrentHashMap<>();
	// 記錄目前房間有哪些人
	private static final ConcurrentHashMap<String, ConcurrentHashMap<String, Session>> roomSessions = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, Set<String>> OnlineUsers = new ConcurrentHashMap<>();

	@OnOpen
	public void onOpen(Session session) throws IOException {
		String token = session.getRequestParameterMap().get("token").get(0);
		session.getUserProperties().put("token", token);
		String username = JwtUtil.getUsernameFromToken(token);

		// 記錄該用戶的 WebSocket 連接
		userSession.put(username, session);
		System.out.println(username + "已建立連線，目前在線人數:" + userSession.size());

		// 查詢用戶擁有的所有聊天室
		List<Long> chatRooms = chatService.findAllChatByUser(username).stream().map(ChatDto::getChatId)
				.collect(Collectors.toList());
		System.out.println(username + "所擁有的聊天室" + chatRooms);
	}

	@OnMessage
	public void onMessage(String customMessage, Session session) throws JsonMappingException, JsonProcessingException {
		System.out.println(customMessage);
		String token = (String) session.getUserProperties().get("token");
		String username = JwtUtil.getUsernameFromToken(token);
		CustomJson<Object> typeMessage = objectMapper.readValue(customMessage, new TypeReference<CustomJson<Object>>() {
		});
		switch (typeMessage.getHead().getType()) {
		case "addRoom":
			typeMessage.getHead().setCondition(CustomJson.generateCondition());// 產生UUID做辨別
			UserToRoom adduser = objectMapper.convertValue(typeMessage.getData(), UserToRoom.class);
			session.getUserProperties().put("roomId", adduser.getRoomId());
			roomSessions.computeIfAbsent(adduser.getRoomId(),k->new ConcurrentHashMap<>()).put(username, session);
			// 用來確保房間存在，若不存在則初始化一個新的 Set
			OnlineUsers.putIfAbsent(adduser.getRoomId(), ConcurrentHashMap.newKeySet()).add(username);
//			OnlineUsers.get(adduser.getRoomId()).add(username);
			System.out.println(adduser.getUsername() + "加入到房間" + adduser.getRoomId());
			System.out.println(
					"目前聊天室 " + adduser.getRoomId() + " 中有 " + roomSessions.get(adduser.getRoomId()).size() + " 人");
			System.out.println(roomSessions.get(adduser.getRoomId()));
			try {
				session.getBasicRemote().sendText(customMessage);
			} catch (IOException e) {
				System.err.println(username + "加入房間失敗!");
			}
			break;
		case "leftRoom":
			typeMessage.getHead().setCondition(CustomJson.generateCondition());// 產生UUID做辨別
			UserToRoom leftuser = objectMapper.convertValue(typeMessage.getData(), UserToRoom.class);
			OnlineUsers.get(leftuser.getRoomId()).remove(username);
			System.out.println(OnlineUsers.get(leftuser.getRoomId()));
			System.out.println(username + " 離開房間 " + leftuser.getRoomId());
			System.out.println(roomSessions.get(leftuser.getRoomId()).isEmpty() ? "目前聊天室沒有人"
					: "目前聊天室 " + leftuser.getRoomId() + " 中剩餘 " + roomSessions.get(leftuser.getRoomId()).size() + " 人");
			System.out.println(roomSessions.get(leftuser.getRoomId()));
			try {
				session.getBasicRemote().sendText(customMessage);
			} catch (IOException e) {
				System.err.println(username + "退出房間失敗!");
			}
			break;
		case "sendMessage":
//			System.out.println(typeMessage);
			typeMessage.getHead().setCondition(CustomJson.generateCondition());// 產生UUID做辨別
			MessageDto messageDto = objectMapper.convertValue(typeMessage.getData(), MessageDto.class);
			Long roomId = messageDto.getReceiveChat().getChatId();
			Set<String> AllUsers = chatService.findAllUserByChat(roomId.toString()).stream()
					.map(user -> user.getUsername()).collect(Collectors.toSet());

			Set<String> offlineUsers = new HashSet<>(AllUsers);
			offlineUsers.removeIf(OnlineUsers.get(roomId.toString())::contains);
			RabbitMQServer.sendMessageToOnlineRabbitMq(customMessage, roomId.toString(), username);
			if (!offlineUsers.isEmpty()) {
				typeMessage.getHead().setType("NotifiedMessage");
				;
				OfflineUser offlineUser = new OfflineUser();
				offlineUser.setOfflineUser(offlineUsers);
				offlineUser.setMessageDto(messageDto);
				typeMessage.setData(offlineUser);
				String NotifiedMessage = objectMapper.writeValueAsString(typeMessage);
				RabbitMQServer.sendMessageToOfflineRabbitMq(NotifiedMessage, roomId.toString(), username);
			}
			break;
		}

	}

	@OnClose
	public void onClose(Session session) {
		String token = (String) session.getUserProperties().get("token");
		String username = JwtUtil.getUsernameFromToken(token);
		String roomId = (String) session.getUserProperties().get("roomId");
		userSession.remove(username);

		if(roomId!=null) {
			roomSessions.get(roomId).remove(username);
			OnlineUsers.get(roomId).remove(username);
			System.out.println(username + " 離開房間 " + roomId);
			System.out.println(roomSessions.get(roomId).isEmpty() ? "目前聊天室沒有人"
					: "目前聊天室 " + roomId + " 中剩餘 " + roomSessions.get(roomId).size() + " 人");
			System.out.println(roomSessions.get(roomId));
		}
		System.out.println("用戶" + username + "關閉連線");
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		String token = (String) session.getUserProperties().get("token");
		String username = JwtUtil.getUsernameFromToken(token);
		String roomId = (String) session.getUserProperties().get("roomId");
		userSession.remove(username);
		if(roomId!=null) {
			roomSessions.get(roomId).remove(username);
			OnlineUsers.get(roomId).remove(username);
			System.out.println(username + " 離開房間 " + roomId);
			System.out.println(roomSessions.get(roomId).isEmpty() ? "目前聊天室沒有人"
					: "目前聊天室 " + roomId + " 中剩餘 " + roomSessions.get(roomId).size() + " 人");
			System.out.println(roomSessions.get(roomId));
		}
		System.err.println("會員 " + username + " 發生錯誤: " + throwable.getMessage());

	}

	// 發送消息到特定房間
	@RabbitListener(queues = "SendMessageToOnlineUsersQueue")
	public void SendMessage(String customMessage) throws IOException {
		CustomJson<Object> tpyeMessage = objectMapper.readValue(customMessage, new TypeReference<CustomJson<Object>>() {
		});
		MessageDto messageDto = objectMapper.convertValue(tpyeMessage.getData(), MessageDto.class);
		Long roomId = messageDto.getReceiveChat().getChatId();
		String username = messageDto.getSendUser().getUsername();
//		messageService.addMessage(messageDto); //將訊息保存在資料庫
		System.out.println("會員 " + username + " 的消費者成功接收到在房間人員的通道訊息 : " + messageDto.getMessage());
		if (roomSessions.containsKey(roomId.toString())) {
			for (Session s : roomSessions.get(roomId.toString()).values()) {
				try {
					s.getBasicRemote().sendText(customMessage);
				} catch (IOException e) {
					System.err.println("接收訊息處理失敗 : " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	@RabbitListener(queues = "SendMessageToOfflineUsersQueue")
	public void receiveMessage(String NotifiedMessage) {
		try {
			CustomJson<Object> tpyeMessage = objectMapper.readValue(NotifiedMessage,
					new TypeReference<CustomJson<Object>>() {
					});
			OfflineUser offlineUser = objectMapper.convertValue(tpyeMessage.getData(), OfflineUser.class);
			String username = offlineUser.getMessageDto().getSendUser().getUsername();
			System.out.println("會員 " + username + " 的消費者成功接收到不在房間者的通道名單和訊息 : " + offlineUser.getOfflineUser() + ":"
					+ offlineUser.getMessageDto().getMessage());
			SendNotifiedMessage(offlineUser.getOfflineUser(), NotifiedMessage);
		} catch (Exception e) {
			System.err.println("接收訊息處理失敗 : " + e.getMessage());
			e.printStackTrace();
		}
	}

	// 發送不在房間訊息通知
	public void SendNotifiedMessage(Set<String> offlineUser, String NotifiedMessage) {
		for (String s : userSession.keySet()) {
			if (offlineUser.contains(s)) {
				try {
					userSession.get(s).getBasicRemote().sendText(NotifiedMessage);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// 發送消息到所有房間(開發給後台使用)
	public void sendMessageToAllRooms(String message) {
		for (ConcurrentHashMap<String, Session> sessions : roomSessions.values()) {
			for (Session s : sessions.values()) {
				try {
					s.getBasicRemote().sendText(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
