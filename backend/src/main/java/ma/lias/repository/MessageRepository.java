package ma.lias.repository;

import ma.lias.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface MessageRepository extends JpaRepository<Message, Long> {
  List<Message> findTop50ByMessageTypeAndDeletedAtIsNullOrderBySentAtDesc(MessageType messageType);
  List<Message> findByEventIdAndMessageTypeAndDeletedAtIsNullOrderBySentAtAsc(Long eventId, MessageType messageType);
  List<Message> findByTeamIdAndMessageTypeAndDeletedAtIsNullOrderBySentAtAsc(Long teamId, MessageType messageType);

  @Query("""
      select m from Message m
      where m.messageType = ma.lias.entity.MessageType.DIRECT
        and m.deletedAt is null
        and ((m.senderId = :first and m.receiverId = :second)
          or (m.senderId = :second and m.receiverId = :first))
      order by m.sentAt asc
      """)
  List<Message> findDirectConversation(@Param("first") Long first, @Param("second") Long second);
}
