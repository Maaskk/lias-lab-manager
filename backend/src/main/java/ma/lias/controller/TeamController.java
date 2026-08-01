package ma.lias.controller;

import ma.lias.entity.Team;
import ma.lias.repository.TeamRepository;
import ma.lias.service.AuditService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController @RequestMapping("/api/teams")
public class TeamController {
  private final TeamRepository repo; private final AuditService audit;
  public TeamController(TeamRepository repo, AuditService audit){this.repo=repo;this.audit=audit;}
  @GetMapping public List<Team> all(){ return repo.findAll(); }
  @GetMapping("/{id}") public Team one(@PathVariable Long id){ return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Introuvable")); }
  @PostMapping public ResponseEntity<Team> create(@RequestBody Team body){ Team saved=repo.save(body); audit.log("CREATE","Team", saved.getId(), "Création"); return ResponseEntity.status(HttpStatus.CREATED).body(saved); }
  @PutMapping("/{id}") public Team update(@PathVariable Long id, @RequestBody Team body){ if(!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Introuvable"); body.setId(id); Team saved=repo.save(body); audit.log("UPDATE","Team", id, "Modification"); return saved; }
  @DeleteMapping("/{id}") public ResponseEntity<?> delete(@PathVariable Long id){ if(!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Introuvable"); repo.deleteById(id); audit.log("DELETE","Team", id, "Suppression"); return ResponseEntity.noContent().build(); }
}
