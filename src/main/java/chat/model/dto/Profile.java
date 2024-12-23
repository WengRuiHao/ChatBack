package chat.model.dto;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class Profile {
	private String username;
	private String nickName;
	private String gender;
	private String email;
	private String profileContent;
	private String vactorPath;
	
	@Override
	public String toString() {
		return "Profile [username=" + username + ", nickName=" + nickName + ", gender=" + gender + ", email=" + email
				+ ", profileContent=" + profileContent + ", vactorPath=" + vactorPath + "]";
	}

	

}
