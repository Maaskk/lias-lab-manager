package ma.lias.controller;

import ma.lias.entity.*;
import ma.lias.repository.*;
import ma.lias.service.AuditService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController @RequestMapping("/api/material")
public class MaterialController {
  private final MaterialInventoryRepository repo; private final MaterialDistributionRepository distributions; private final MemberRepository members; private final AuditService audit;
  public MaterialController(MaterialInventoryRepository repo, MaterialDistributionRepository distributions, MemberRepository members, AuditService audit){this.repo=repo;this.distributions=distributions;this.members=members;this.audit=audit;}
  @GetMapping public List<MaterialInventory> all(){ return repo.findAll(); }
  @GetMapping("/equity") public Map<String,Object> equity(){ Set<Long> recipients=distributions.findAll().stream().map(MaterialDistribution::getMemberId).filter(Objects::nonNull).collect(java.util.stream.Collectors.toSet()); List<Map<String,Object>> without=members.findAll().stream().filter(m -> m.getType()!=MemberType.FORMER && m.getType()!=MemberType.RETIRED).filter(m -> !recipients.contains(m.getId())).map(this::safeMember).toList(); return Map.of("totalActiveMembers",members.findAll().stream().filter(m -> m.getType()!=MemberType.FORMER && m.getType()!=MemberType.RETIRED).count(),"membersWithMaterial",recipients.size(),"membersWithoutMaterial",without); }
  @GetMapping("/{id}") public MaterialInventory one(@PathVariable Long id){ return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Introuvable")); }
  @PostMapping public ResponseEntity<MaterialInventory> create(@RequestBody MaterialInventory body){ MaterialInventory saved=repo.save(body); audit.log("CREATE","MaterialInventory", saved.getId(), "Création"); return ResponseEntity.status(HttpStatus.CREATED).body(saved); }
  @PutMapping("/{id}") public MaterialInventory update(@PathVariable Long id, @RequestBody MaterialInventory body){ if(!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Introuvable"); body.setId(id); MaterialInventory saved=repo.save(body); audit.log("UPDATE","MaterialInventory", id, "Modification"); return saved; }
  @DeleteMapping("/{id}") public ResponseEntity<?> delete(@PathVariable Long id){ if(!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Introuvable"); repo.deleteById(id); audit.log("DELETE","MaterialInventory", id, "Suppression"); return ResponseEntity.noContent().build(); }
  private Map<String,Object> safeMember(Member member){ Map<String,Object> out=new LinkedHashMap<>(); out.put("id",member.getId()); out.put("firstName",member.getFirstName()); out.put("lastName",member.getLastName()); out.put("type",member.getType()); out.put("establishment",member.getEstablishment()); out.put("currentTeamId",member.getCurrentTeamId()); return out; }
}
