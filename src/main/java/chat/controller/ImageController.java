package chat.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import chat.response.ApiResponse;
import chat.service.UserService;

@RestController
@RequestMapping("/file")
public class ImageController {

	private static final String Profile_Image = "uploads/profiles/";
	
	@Autowired
	private UserService userService;
	
    // 初始化目錄
    static {
        try {
            Files.createDirectories(Paths.get(Profile_Image));
        } catch (IOException e) {
            throw new RuntimeException("初始化目錄失敗：" + e.getMessage());
        }
    }

//	@PutMapping("/User/ImageUpload")
//	public ResponseEntity<ApiResponse<?>> uploadImage(@RequestParam("File") MultipartFile file,@AuthenticationPrincipal String username) {
//		String fileName = file.getOriginalFilename();
//		
//		try {
//			Path folderPath = Paths.get(Profile_Image);
//			Path filePath = folderPath.resolve(fileName);
//			//System.out.println(filePath.toString());
//			userService.addProfileImage(filePath.toString(),username);
//			 Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//			 return ResponseEntity.ok(ApiResponse.success("檔案已成功上傳至：" + filePath.toAbsolutePath(), null));
//		} catch (Exception e) {
//			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//					.body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "檔案上傳失敗：" + e.getMessage()));
//		}
//	}
	
//	@GetMapping("/User/ImageUpload")
//	public ResponseEntity<ApiResponse<byte[]>> getImage(@AuthenticationPrincipal String username) throws IOException {
//		 File file=new File(userService.getProfileImage(username));
//		// 讀取文件為 byte[]
//		byte[] fileContent = Files.readAllBytes(file.toPath());
//		return ResponseEntity.ok(ApiResponse.success("檔案查詢成功", fileContent));
//	}
//
//	@PutMapping("/User/ImageUpload")
//	public ResponseEntity<ApiResponse<?>> updateImage(@AuthenticationPrincipal String username,String ImageUrl) throws IOException {
//		System.out.println(ImageUrl);
//		return ResponseEntity.ok(ApiResponse.success("檔案查詢成功", null));
//	}
}
