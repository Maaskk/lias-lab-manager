package ma.lias.controller;

import ma.lias.entity.*;
import ma.lias.repository.*;
import ma.lias.service.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController @RequestMapping("/api/members")
public class MemberController {
  private final MemberRepository members; private final UserRepository users; private final AffiliationRepository affiliations; private final RoleHistoryRepository roles; private final TeamRepository teams; private final PublicationRepository publications; private final AuditService audit; private final FileStorageService storage; private final MemberLifecycleService lifecycle;
  public MemberController(MemberRepository members, UserRepository users, AffiliationRepository affiliations, RoleHistoryRepository roles, TeamRepository teams, PublicationRepository publications, AuditService audit, FileStorageService storage, MemberLifecycleService lifecycle){this.members=members;this.users=users;this.affiliations=affiliations;this.roles=roles;this.teams=teams;this.publications=publications;this.audit=audit;this.storage=storage;this.lifecycle=lifecycle;}
  @GetMapping public List<Map<String,Object>> all(Authentication auth){ boolean admin=isAdmin(auth); return members.findAll().stream().map(m -> map(m,admin)).toList(); }
  @GetMapping("/{id}") public Map<String,Object> one(@PathVariable Long id, Authentication auth){ Member m=members.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Membre introuvable")); Map<String,Object> out=map(m,isAdmin(auth)); out.put("affiliations", affiliations.findAll().stream().filter(a -> Objects.equals(a.getMemberId(), id)).toList()); out.put("roles", roles.findAll().stream().filter(r -> Objects.equals(r.getMemberId(), id)).toList()); if(m.getUserId()!=null) out.put("publications", publications.findByAddedByOrderByYearDescCreatedAtDesc(m.getUserId())); return out; }
  @GetMapping("/me") public Map<String,Object> me(Authentication auth){ User u=(User)auth.getPrincipal(); Member m=members.findByUserId(u.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Profil introuvable")); return one(m.getId(), auth); }
  @PostMapping public ResponseEntity<Member> create(@RequestBody Member body, Authentication auth){ if(!isManager(auth)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Création réservée à la direction ou à l'administration"); Member saved=members.save(body); lifecycle.openInitialAffiliation(saved); audit.log("CREATE","Member",saved.getId(),"Création membre"); return ResponseEntity.status(HttpStatus.CREATED).body(saved); }
  @PutMapping("/{id}") public Member update(@PathVariable Long id, @RequestBody Member body, Authentication auth){
    Member existing=members.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Membre introuvable"));
    if(!canEdit(existing, auth)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Modification non autorisée");
    String before="name="+existing.getFirstName()+" "+existing.getLastName()+",team="+existing.getCurrentTeamId()+",type="+existing.getType();
    MemberType requestedType = body.getType();
    existing.setPhotoUrl(body.getPhotoUrl());
    existing.setFirstName(body.getFirstName());
    existing.setLastName(body.getLastName());
    existing.setBiography(body.getBiography());
    existing.setInterests(body.getInterests());
    existing.setEstablishment(body.getEstablishment());
    existing.setOriginLab(body.getOriginLab());
    existing.setHireDate(body.getHireDate());
    if(isManager(auth)){
      existing.setBirthDate(body.getBirthDate());
      existing.setCurrentTeamId(body.getCurrentTeamId());
      existing.setAffiliationDate(body.getAffiliationDate());
    }
    Member saved=members.save(existing);
    if(isManager(auth) && requestedType!=null && requestedType!=saved.getType()) saved=lifecycle.changeType(id, requestedType);
    String after="name="+saved.getFirstName()+" "+saved.getLastName()+",team="+saved.getCurrentTeamId()+",type="+saved.getType();
    audit.log("UPDATE","Member",id,"Profil modifié | avant: "+before+" | après: "+after);
    return saved;
  }
  @PostMapping("/{id}/photo") public Member photo(@PathVariable Long id, @RequestParam MultipartFile file, Authentication auth) throws Exception { Member m=members.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Membre introuvable")); if(!canEdit(m, auth)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Modification non autorisée"); m.setPhotoUrl(storage.save(file,"photos")); audit.log("UPLOAD","Member",id,"Photo mise à jour"); return members.save(m); }
  @PatchMapping("/{id}/type") public Member changeType(@PathVariable Long id, @RequestParam MemberType type, Authentication auth){ if(!isManager(auth)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Changement de statut réservé à la direction ou à l'administration"); return lifecycle.changeType(id,type); }
  @PatchMapping("/{id}/retire") public Member retire(@PathVariable Long id, Authentication auth){ if(!isManager(auth)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Retraite réservée à la direction ou à l'administration"); return lifecycle.retire(id); }
  @PatchMapping("/{id}/mark-former") public Member markFormer(@PathVariable Long id, Authentication auth){ if(!isManager(auth)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Départ réservé à la direction ou à l'administration"); return lifecycle.markFormer(id); }
  @PatchMapping("/{id}/reactivate") public Member reactivate(@PathVariable Long id, @RequestParam(required=false) MemberType type, Authentication auth){ if(!isManager(auth)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Réactivation réservée à la direction ou à l'administration"); return lifecycle.reactivate(id,type); }
  private boolean isAdmin(Authentication auth){ return auth!=null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")); }
  private boolean isManager(Authentication auth){ return auth!=null && auth.getAuthorities().stream().anyMatch(a -> Set.of("ROLE_ADMIN","ROLE_DIRECTOR","ROLE_VICE_DIRECTOR").contains(a.getAuthority())); }
  private boolean canEdit(Member m, Authentication auth){ if(isManager(auth)) return true; return auth!=null && auth.getPrincipal() instanceof User u && Objects.equals(m.getUserId(), u.getId()); }
  private Map<String,Object> map(Member m, boolean admin){ Map<String,Object> x=new LinkedHashMap<>(); x.put("id",m.getId());x.put("userId",m.getUserId()); users.findById(m.getUserId()==null?-1L:m.getUserId()).ifPresent(u -> x.put("email",u.getEmail())); x.put("firstName",m.getFirstName());x.put("lastName",m.getLastName());x.put("photoUrl",m.getPhotoUrl());x.put("hireDate",m.getHireDate());x.put("affiliationDate",m.getAffiliationDate());x.put("biography",m.getBiography());x.put("interests",m.getInterests());x.put("establishment",m.getEstablishment());x.put("originLab",m.getOriginLab());x.put("currentTeamId",m.getCurrentTeamId()); teams.findById(m.getCurrentTeamId()==null?-1L:m.getCurrentTeamId()).ifPresent(t -> x.put("teamName",t.getName())); x.put("type",m.getType()); if(admin) x.put("birthDate",m.getBirthDate()); return x; }
}
