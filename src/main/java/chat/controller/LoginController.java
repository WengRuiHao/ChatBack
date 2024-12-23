package chat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import chat.model.dto.Register;
import chat.model.dto.SendEmailDto;
import chat.response.ApiResponse;
import chat.service.UserService;
import chat.util.JwtUtil;

@RestController
@RequestMapping("/Login")
public class LoginController {

	@Autowired
	private UserService userService;

	@PostMapping
	public ResponseEntity<ApiResponse<?>> checkPassword(@RequestBody Register register) {
		System.out.println(register.toString());
		if (userService.checkPassword(register).isPresent()) {
			String token = JwtUtil.generateToken(register.getUsername());
			return ResponseEntity.ok(ApiResponse.success("登入成功", token));
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "用戶名或密碼錯誤"));

	}
	
	@PostMapping("/Forget")
	public ResponseEntity<ApiResponse<?>> sendEmail(@RequestBody SendEmailDto sendEmailDto){
		userService.sendEmail(sendEmailDto);
		return ResponseEntity.ok(ApiResponse.success("提交成功", null));
	}
	
	@PutMapping("/password")
	public ResponseEntity<ApiResponse<?>> forgetPassword(@AuthenticationPrincipal String username,@RequestBody Register register){
		System.out.println(register.getPassword());
		userService.forgetPassword(username,register.getPassword());
		return ResponseEntity.ok(ApiResponse.success("修改成功", null));
	}

	// 處理異常狀況
	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<ApiResponse<Void>> handleTodoRuntimeException(RuntimeException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage()));
	}

}
