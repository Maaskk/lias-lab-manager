package ma.lias.controller;

import ma.lias.entity.Mandate;
import ma.lias.repository.MandateRepository;
import ma.lias.service.ActiveMandateService;
import ma.lias.service.AuditService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController @RequestMapping("/api/admin/mandates")
public class MandateController {
  private final MandateRepository repo; private final AuditService audit; private final ActiveMandateService activeMandates;
  public MandateController(MandateRepository repo, AuditService audit, ActiveMandateService activeMandates){this.repo=repo;this.audit=audit;this.activeMandates=activeMandates;}
  @GetMapping public List<Mandate> all(){ return repo.findAll(); }
  @GetMapping("/{id}") public Mandate one(@PathVariable Long id){ return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Introuvable")); }
  @PostMapping public ResponseEntity<Mandate> create(@RequestBody Mandate body){ activeMandates.validateNoOverlap(body,null); Mandate saved=repo.save(body); activeMandates.syncMandateRoles(saved); audit.log("CREATE","Mandate", saved.getId(), "Création avec contrôle de chevauchement"); return ResponseEntity.status(HttpStatus.CREATED).body(saved); }
  @PutMapping("/{id}") public Mandate update(@PathVariable Long id, @RequestBody Mandate body){ if(!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Introuvable"); activeMandates.validateNoOverlap(body,id); body.setId(id); Mandate saved=repo.save(body); activeMandates.syncMandateRoles(saved); audit.log("UPDATE","Mandate", id, "Modification avec historisation des rôles"); return saved; }
  @DeleteMapping("/{id}") public ResponseEntity<?> delete(@PathVariable Long id){ Mandate mandate=one(id); if(mandate.getEndDate()==null) mandate.setEndDate(java.time.LocalDate.now()); repo.save(mandate); audit.log("ARCHIVE","Mandate", id, "Mandat clôturé au lieu d'une suppression destructive"); return ResponseEntity.noContent().build(); }
}
