package ma.lias.controller;

import ma.lias.entity.*;
import ma.lias.repository.*;
import ma.lias.service.AuditService;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController @RequestMapping("/api/publications")
public class PublicationController {
  private final PublicationRepository repo; private final MemberRepository members; private final AuditService audit;
  public PublicationController(PublicationRepository repo, MemberRepository members, AuditService audit){this.repo=repo;this.members=members;this.audit=audit;}
  @GetMapping public List<Publication> all(@RequestParam(required=false) Integer year, @RequestParam(required=false) Long teamId, @RequestParam(required=false) String author){ return repo.findAll().stream().filter(p -> year==null || Objects.equals(p.getYear(), year)).filter(p -> teamId==null || Objects.equals(p.getTeamId(), teamId)).filter(p -> author==null || (p.getAuthors()!=null && p.getAuthors().toLowerCase(Locale.ROOT).contains(author.toLowerCase(Locale.ROOT)))).toList(); }
  @GetMapping("/{id}") public Publication one(@PathVariable Long id){ return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Introuvable")); }
  @PostMapping public ResponseEntity<Publication> create(@RequestBody Publication body, Authentication auth){ if(auth!=null && auth.getPrincipal() instanceof User u){ Optional<Member> actorMember=members.findByUserId(u.getId()); if(actorMember.isPresent()){ Member m=actorMember.get(); body.setAddedBy(m.getId()); } else if(!canManage(auth)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Profil membre requis pour déposer une publication"); } Publication saved=repo.save(body); audit.log("CREATE","Publication", saved.getId(), "Création par utilisateur authentifié"); return ResponseEntity.status(HttpStatus.CREATED).body(saved); }
  @PutMapping("/{id}") public Publication update(@PathVariable Long id, @RequestBody Publication body, Authentication auth){ Publication existing=one(id); if(!canModify(existing, auth)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Modification réservée au déposant ou à la direction"); body.setId(id); body.setAddedBy(existing.getAddedBy()); body.setCreatedAt(existing.getCreatedAt()); Publication saved=repo.save(body); audit.log("UPDATE","Publication", id, "Modification"); return saved; }
  @DeleteMapping("/{id}") public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth){ Publication existing=one(id); if(!canManage(auth)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Suppression réservée à la direction"); repo.deleteById(id); audit.log("DELETE","Publication", id, "Suppression publication"); return ResponseEntity.noContent().build(); }
  private boolean canModify(Publication p, Authentication auth){ if(canManage(auth)) return true; return auth!=null && auth.getPrincipal() instanceof User u && members.findByUserId(u.getId()).map(m -> Objects.equals(p.getAddedBy(),m.getId())).orElse(false); }
  private boolean canManage(Authentication auth){ return auth!=null && auth.getAuthorities().stream().anyMatch(a -> Set.of("ROLE_ADMIN","ROLE_DIRECTOR","ROLE_VICE_DIRECTOR","ROLE_TEAM_CHIEF").contains(a.getAuthority())); }
}
