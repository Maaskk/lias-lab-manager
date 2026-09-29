package ma.lias.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/public/health")
public class HealthController {
  private final JdbcTemplate database;

  public HealthController(JdbcTemplate database) {
    this.database = database;
  }

  @GetMapping
  public Map<String, Object> health() {
    Integer check = database.queryForObject("select 1", Integer.class);
    return Map.of("status", check != null && check == 1 ? "UP" : "DOWN", "timestamp", Instant.now());
  }
}
