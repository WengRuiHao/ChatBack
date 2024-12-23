package chat.customJson;

import java.util.Set;

import chat.model.dto.MessageDto;
import lombok.Data;

@Data
public class OfflineUser {

	private Set<String> OfflineUser;
	private MessageDto messageDto;
}
