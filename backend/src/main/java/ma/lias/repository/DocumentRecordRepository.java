package ma.lias.repository;
import ma.lias.entity.DocumentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface DocumentRecordRepository extends JpaRepository<DocumentRecord, Long> {
  List<DocumentRecord> findByEventId(Long eventId);
}
