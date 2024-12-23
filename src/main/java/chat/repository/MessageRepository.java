package chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import chat.model.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

	@Query("select m from Message m where m.receiveChat.chatId =:chatId order by m.createAt ASC")
	List<Message> findAllByRoomId(@Param("chatId") Long chatId);
}
