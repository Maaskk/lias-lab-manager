package ma.lias.repository;
import ma.lias.entity.MaterialInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface MaterialInventoryRepository extends JpaRepository<MaterialInventory, Long> {
  Optional<MaterialInventory> findFirstByNameIgnoreCase(String name);
}
