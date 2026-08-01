package ma.lias.repository;
import ma.lias.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface MemberRepository extends JpaRepository<Member, Long> {
  Optional<Member> findByUserId(Long userId);
  List<Member> findByCurrentTeamId(Long currentTeamId);
}
