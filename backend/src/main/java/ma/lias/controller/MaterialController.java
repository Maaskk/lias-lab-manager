package ma.lias.controller;

import ma.lias.entity.MaterialInventory;
import ma.lias.repository.MaterialInventoryRepository;
import ma.lias.service.AuditService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController @RequestMapping("/api/material")
public class MaterialController {
  private final MaterialInventoryRepository repo; private final AuditService audit;
  public MaterialController(MaterialInventoryRepository repo, AuditService audit){this.repo=repo;this.audit=audit;}
  @GetMapping public List<MaterialInventory> all(){ return repo.findAll(); }
  @GetMapping("/{id}") public MaterialInventory one(@PathVariable Long id){ return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Introuvable")); }
  @PostMapping public ResponseEntity<MaterialInventory> create(@RequestBody MaterialInventory body){ MaterialInventory saved=repo.save(body); audit.log("CREATE","MaterialInventory", saved.getId(), "Création"); return ResponseEntity.status(HttpStatus.CREATED).body(saved); }
  @PutMapping("/{id}") public MaterialInventory update(@PathVariable Long id, @RequestBody MaterialInventory body){ if(!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Introuvable"); body.setId(id); MaterialInventory saved=repo.save(body); audit.log("UPDATE","MaterialInventory", id, "Modification"); return saved; }
  @DeleteMapping("/{id}") public ResponseEntity<?> delete(@PathVariable Long id){ if(!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Introuvable"); repo.deleteById(id); audit.log("DELETE","MaterialInventory", id, "Suppression"); return ResponseEntity.noContent().build(); }
}
