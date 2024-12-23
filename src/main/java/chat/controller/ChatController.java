package chat.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import chat.model.dto.ChatDto;
import chat.model.dto.ChatroomDto;
import chat.model.dto.MessageDto;
import chat.model.dto.UserDto;
import chat.model.entity.User;
import chat.model.request.AddUserRequest;
import chat.model.request.DeleteChatRequest;
import chat.model.request.LeaveChatRequest;
import chat.response.ApiResponse;
import chat.service.ChatService;
import chat.service.MessageService;
import chat.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/home/chat")
public class ChatController {

	@Autowired
	private MessageService messageService;

	@Autowired
	private UserService userService;

	@Autowired
	private ChatService chatService;
	
	@PostMapping// **1. 創建聊天室**
	public ResponseEntity<ApiResponse<ChatDto>> createChat(@RequestBody ChatDto chatDto,@AuthenticationPrincipal String username){
		chatService.createChat(chatDto, username);
		return ResponseEntity.ok(ApiResponse.success("創建成功", chatDto));
	}
	
	@GetMapping("/user") //查詢會員的所有聊天室
	public ResponseEntity<ApiResponse<List<ChatDto>>> findAllChatByUser(@AuthenticationPrincipal String username) {
		List<ChatDto> chats=chatService.findAllChatByUser(username);
		return ResponseEntity.ok(ApiResponse.success("獲取聊天室成功", chats));
	}
	
	@PostMapping("/addUser")
	public ResponseEntity<ApiResponse<ChatroomDto>> addUserToChat(@RequestBody AddUserRequest request,@AuthenticationPrincipal String currentUsername) {
		// 驗證請求參數是否完整
		if (request.getChatId() == null || request.getUsername() == null || request.getUsername().trim().isEmpty()) {
			return ResponseEntity.badRequest().body(ApiResponse.error(400, "請求參數不完整"));
		}
		// 調用服務層，執行將目標用戶加入聊天室的邏輯
		ChatroomDto updatedChatDto = chatService.addUserToChat(request.getChatId(), request.getUsername());
		// 返回成功響應
		return ResponseEntity.ok(ApiResponse.success("用戶成功加入聊天室", updatedChatDto));
	}
	
	// **5. 用戶退出聊天室**
	@PostMapping("/leave")
	public ResponseEntity<ApiResponse<ChatroomDto>> leaveChat(@RequestBody LeaveChatRequest request,@AuthenticationPrincipal String username) {
		// 驗證請求參數是否完整
		if (request.getChatId() == null || request.getUsername() == null || request.getUsername().trim().isEmpty()) {
			return ResponseEntity.badRequest().body(ApiResponse.error(400, "請求參數不完整"));
		}
		// 調用服務層，執行用戶離開聊天室的邏輯
		ChatroomDto updatedChatDto = chatService.leaveChat(request.getChatId(), request.getUsername());
		// 返回成功響應
		return ResponseEntity.ok(ApiResponse.success("用戶成功離開聊天室", updatedChatDto));
	}
	
	@GetMapping("/{roomId}") // 取得房間的所有聊天紀錄
	public ResponseEntity<ApiResponse<List<MessageDto>>> getChatHistory(@PathVariable String roomId) {
		messageService.getAllMessage(roomId);
		return ResponseEntity.ok(ApiResponse.success("查詢成功", messageService.getAllMessage(roomId)));
	}

	@GetMapping  //取得個人資訊
	public ResponseEntity<ApiResponse<UserDto>> getProfile(@AuthenticationPrincipal String username) {
		return ResponseEntity.ok(ApiResponse.success("查詢成功", userService.findByUser(username)));
	}

	@GetMapping("/{roomId}/profile") //取得房間資訊
	public ResponseEntity<ApiResponse<ChatDto>> getChat(@PathVariable String roomId) {
		return ResponseEntity.ok(ApiResponse.success("查詢成功", chatService.getChat(roomId)));
	}
	
	@DeleteMapping("/{roomId}")  //刪除房間
	public ResponseEntity<ApiResponse<ChatDto>> deleteChat(@RequestBody DeleteChatRequest request,@PathVariable String roomId){
		// 驗證請求參數是否完整
		if (request.getChatId() == null) {
			return ResponseEntity.badRequest().body(ApiResponse.error(400, "請求參數不完整"));
		}
		chatService.deleteChat(roomId);
		return ResponseEntity.ok(ApiResponse.success("刪除成功", null));
	}

	// 處理異常狀況
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiResponse<Void>> handleTodoRuntimeException(RuntimeException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
	}

}
