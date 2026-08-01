package ma.lias.repository;
import ma.lias.entity.MaterialDistribution;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface MaterialDistributionRepository extends JpaRepository<MaterialDistribution, Long> {
  List<MaterialDistribution> findByMemberId(Long memberId);
}
