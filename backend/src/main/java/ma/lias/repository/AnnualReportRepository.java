package ma.lias.repository;
import ma.lias.entity.AnnualReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnualReportRepository extends JpaRepository<AnnualReport, Long> {
}
