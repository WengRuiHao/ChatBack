package chat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendEmailDto {

	private String username;
	private String email;
	
	@Override
	public String toString() {
		return "SendEmailDto [username=" + username + ", email=" + email + "]";
	}
}
