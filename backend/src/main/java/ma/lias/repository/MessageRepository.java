package ma.lias.repository;
import ma.lias.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface MessageRepository extends JpaRepository<Message, Long> {
  List<Message> findTop50ByOrderBySentAtDesc();
  List<Message> findByEventIdOrderBySentAtAsc(Long eventId);
}
