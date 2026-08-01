package ma.lias.repository;
import ma.lias.entity.Convention;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConventionRepository extends JpaRepository<Convention, Long> {
}
