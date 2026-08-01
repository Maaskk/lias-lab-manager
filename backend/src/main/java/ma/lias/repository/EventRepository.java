package ma.lias.repository;
import ma.lias.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface EventRepository extends JpaRepository<Event, Long> {
  List<Event> findTop10ByOrderByStartDateDesc();
}
