package chat.websocket;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import chat.customJson.CustomJson;
import chat.customJson.OfflineUser;
import chat.model.dto.MessageDto;

@Service
public class RabbitMQServer {

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	// 傳送給在房間人員的訊息
	public void sendMessageToOnlineRabbitMq(String customMessage, String roomId, String username)
			throws JsonMappingException, JsonProcessingException {
		try {
			CustomJson<Object> typeMessage = objectMapper.readValue(customMessage,
					new TypeReference<CustomJson<Object>>() {
					});
			MessageDto messageDto = objectMapper.convertValue(typeMessage.getData(), MessageDto.class);
			System.out.println("會員 " + username + " 從房間 " + roomId + " 的生產者訊息 : " + messageDto.getMessage());
			System.out.println("準備發送到處理在房間會員訊息的通道...");
			rabbitTemplate.convertAndSend("TypeExchange", "SendMessageToOnlineUsers", customMessage);
		} catch (Exception e) {
			// 處理訊息發送異常
			System.err.println("訊息發送失敗: " + e.getMessage());
			throw new RuntimeException("Failed to send message to RabbitMQ", e);
		}
	}

	// 傳送給不在房間人員的訊息
	public void sendMessageToOfflineRabbitMq(String NotifiedMessage, String roomId, String username) {
		try {
			CustomJson<Object> typeMessage = objectMapper.readValue(NotifiedMessage,
					new TypeReference<CustomJson<Object>>() {
					});
			OfflineUser offlineUser = objectMapper.convertValue(typeMessage.getData(), OfflineUser.class);
			System.out.println("會員 " + username + " 從房間 " + roomId + " 的生產者不在房間名單和訊息 : " + offlineUser.getOfflineUser()
					+ ":" + offlineUser.getMessageDto().getMessage());
			System.out.println("準備發送到處理不在房間會員訊息的通道...");
			rabbitTemplate.convertAndSend("TypeExchange", "SendMessageToOfflineUsers", NotifiedMessage);
		} catch (Exception e) {
			// 處理訊息發送異常
			System.err.println("訊息發送失敗: " + e.getMessage());
			throw new RuntimeException("Failed to send message to RabbitMQ", e);
		}
	}

}
