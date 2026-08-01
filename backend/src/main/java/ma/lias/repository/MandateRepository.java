package ma.lias.repository;
import ma.lias.entity.Mandate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MandateRepository extends JpaRepository<Mandate, Long> {
}
