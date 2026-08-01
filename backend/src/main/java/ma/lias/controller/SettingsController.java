package ma.lias.controller;

import ma.lias.entity.SystemSetting;
import ma.lias.repository.SystemSettingRepository;
import ma.lias.service.AuditService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController @RequestMapping("/api/admin/settings")
public class SettingsController {
  private final SystemSettingRepository repo; private final AuditService audit;
  public SettingsController(SystemSettingRepository repo, AuditService audit){this.repo=repo;this.audit=audit;}
  @GetMapping public List<SystemSetting> all(){ return repo.findAll(); }
  @GetMapping("/{id}") public SystemSetting one(@PathVariable Long id){ return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Introuvable")); }
  @PostMapping public ResponseEntity<SystemSetting> create(@RequestBody SystemSetting body){ SystemSetting saved=repo.save(body); audit.log("CREATE","SystemSetting", saved.getId(), "Création"); return ResponseEntity.status(HttpStatus.CREATED).body(saved); }
  @PutMapping("/{id}") public SystemSetting update(@PathVariable Long id, @RequestBody SystemSetting body){ if(!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Introuvable"); body.setId(id); SystemSetting saved=repo.save(body); audit.log("UPDATE","SystemSetting", id, "Modification"); return saved; }
  @DeleteMapping("/{id}") public ResponseEntity<?> delete(@PathVariable Long id){ if(!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Introuvable"); repo.deleteById(id); audit.log("DELETE","SystemSetting", id, "Suppression"); return ResponseEntity.noContent().build(); }
}
