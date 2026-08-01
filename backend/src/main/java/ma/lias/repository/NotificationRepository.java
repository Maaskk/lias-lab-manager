package ma.lias.repository;
import ma.lias.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
  long countByUserIdAndReadFalse(Long userId);
}
