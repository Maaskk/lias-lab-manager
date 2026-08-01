package ma.lias.controller;

import ma.lias.dto.CreateUserRequest;
import ma.lias.entity.*;
import ma.lias.repository.*;
import ma.lias.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;

@RestController @RequestMapping("/api/admin") @PreAuthorize("hasRole('ADMIN')")
public class AdminController {
  private final UserRepository users; private final AuditLogRepository auditLog; private final PasswordEncoder encoder; private final AuditService audit; private final MemberRepository members; private final RoleHistoryRepository roleHistory;
  public AdminController(UserRepository users, AuditLogRepository auditLog, PasswordEncoder encoder, AuditService audit, MemberRepository members, RoleHistoryRepository roleHistory){this.users=users;this.auditLog=auditLog;this.encoder=encoder;this.audit=audit;this.members=members;this.roleHistory=roleHistory;}
  @GetMapping public Map<String,Object> dashboard(){ return Map.of("users",users.count(),"audit",auditLog.findAll().stream().sorted((a,b)->b.getTimestamp().compareTo(a.getTimestamp())).limit(20).toList()); }
  @GetMapping("/users") public List<User> users(){ return users.findAll(); }
  @PostMapping("/users") public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest req){ User u=new User(); u.setEmail(req.email()); u.setPasswordHash(encoder.encode(req.password())); u.setAppRole(req.appRole()==null?AppRole.PERMANENT_MEMBER:req.appRole()); u.setStatus(req.status()==null?UserStatus.ACTIVE:req.status()); User saved=users.save(u); audit.log("CREATE","User",saved.getId(),"Utilisateur créé"); return ResponseEntity.status(HttpStatus.CREATED).body(saved); }
  @PatchMapping("/users/{id}/status") public User status(@PathVariable Long id, @RequestParam UserStatus status){ User u=users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Utilisateur introuvable")); UserStatus old=u.getStatus(); u.setStatus(status); audit.log("STATUS_CHANGE","User",id,"Statut utilisateur: "+old+" -> "+status); return users.save(u); }
  @PatchMapping("/users/{id}/role") public User role(@PathVariable Long id, @RequestParam AppRole role){ User u=users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Utilisateur introuvable")); AppRole old=u.getAppRole(); u.setAppRole(role); if(role==AppRole.RETIRED) u.setStatus(UserStatus.FROZEN); if(role==AppRole.FORMER) u.setStatus(UserStatus.DISABLED); users.save(u); members.findByUserId(id).ifPresent(m -> recordRoleHistory(m.getId(), role)); audit.log("ROLE_CHANGE","User",id,"Rôle utilisateur: "+old+" -> "+role); return u; }
  @PatchMapping("/users/{id}/password") public User resetPassword(@PathVariable Long id, @RequestParam String password){ User u=users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Utilisateur introuvable")); u.setPasswordHash(encoder.encode(password)); audit.log("PASSWORD_RESET","User",id,"Mot de passe réinitialisé"); return users.save(u); }
  @GetMapping("/audit") public List<AuditLog> audit(){ return auditLog.findAll(); }
  private void recordRoleHistory(Long memberId, AppRole appRole){ RoleName role = switch(appRole){ case DIRECTOR -> RoleName.DIRECTOR; case VICE_DIRECTOR -> RoleName.VICE_DIR; case TEAM_CHIEF -> RoleName.TEAM_CHIEF; default -> RoleName.MEMBER; }; roleHistory.findAll().stream().filter(r -> Objects.equals(r.getMemberId(), memberId) && r.getEndDate()==null).forEach(r -> { r.setEndDate(LocalDate.now()); roleHistory.save(r); }); RoleHistory entry=new RoleHistory(); entry.setMemberId(memberId); entry.setRole(role); entry.setStartDate(LocalDate.now()); roleHistory.save(entry); }
}
