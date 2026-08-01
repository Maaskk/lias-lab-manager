package ma.lias.controller;

import ma.lias.entity.*;
import ma.lias.repository.*;
import ma.lias.service.AuditService;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;

@RestController @RequestMapping("/api/material-requests")
public class MaterialRequestController {
  private final MaterialRequestRepository repo; private final MaterialInventoryRepository inventory; private final MaterialDistributionRepository distributions; private final MemberRepository members; private final NotificationRepository notifications; private final AuditService audit;
  public MaterialRequestController(MaterialRequestRepository repo, MaterialInventoryRepository inventory, MaterialDistributionRepository distributions, MemberRepository members, NotificationRepository notifications, AuditService audit){this.repo=repo;this.inventory=inventory;this.distributions=distributions;this.members=members;this.notifications=notifications;this.audit=audit;}
  @GetMapping public List<MaterialRequest> all(org.springframework.security.core.Authentication auth){ if(auth!=null && auth.getPrincipal() instanceof User u && auth.getAuthorities().stream().noneMatch(a -> Set.of("ROLE_ADMIN","ROLE_DIRECTOR","ROLE_VICE_DIRECTOR").contains(a.getAuthority()))) return repo.findAll().stream().filter(r -> Objects.equals(r.getRequestedBy(), u.getId())).toList(); return repo.findAll();}
  @GetMapping("/{id}") public MaterialRequest one(@PathVariable Long id){return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Demande introuvable"));}
  @PostMapping public ResponseEntity<MaterialRequest> create(@RequestBody MaterialRequest body, org.springframework.security.core.Authentication auth){ if(auth!=null && auth.getPrincipal() instanceof User u) body.setRequestedBy(u.getId()); MaterialRequest saved=repo.save(body); audit.log("CREATE","MaterialRequest",saved.getId(),"Demande matériel"); return ResponseEntity.status(HttpStatus.CREATED).body(saved); }
  @PostMapping("/{id}/decision") @PreAuthorize("hasAnyRole('DIRECTOR','VICE_DIRECTOR','ADMIN')") public MaterialRequest decision(@PathVariable Long id, @RequestParam MaterialRequestStatus status, @RequestParam(required=false) String reason, org.springframework.security.core.Authentication auth){
    MaterialRequest r=one(id);
    if(r.getStatus()!=MaterialRequestStatus.PENDING) throw new ResponseStatusException(HttpStatus.CONFLICT,"Cette demande a déjà été traitée.");
    User reviewer = auth!=null && auth.getPrincipal() instanceof User u ? u : null;
    r.setStatus(status);
    r.setReviewReason(reason);
    if(reviewer!=null) r.setReviewedBy(reviewer.getId());
    r.setReviewedAt(LocalDateTime.now());
    if(status==MaterialRequestStatus.APPROVED) distributeApprovedRequest(r, reviewer);
    notifyRequester(r, status==MaterialRequestStatus.APPROVED ? "Votre demande de matériel a été acceptée." : "Votre demande de matériel a été refusée.");
    audit.log("DECISION","MaterialRequest",id,"Décision: "+status+(reason==null?"":" | motif: "+reason));
    return repo.save(r);
  }
  private void distributeApprovedRequest(MaterialRequest request, User reviewer){
    MaterialInventory item=inventory.findFirstByNameIgnoreCase(request.getMaterialName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,"Aucun stock ne correspond au matériel demandé."));
    if(request.getQuantity()<=0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"La quantité demandée doit être positive.");
    if(item.getQuantity()<request.getQuantity()) throw new ResponseStatusException(HttpStatus.CONFLICT,"Stock insuffisant pour distribuer ce matériel.");
    item.setQuantity(item.getQuantity()-request.getQuantity());
    inventory.save(item);
    MaterialDistribution d=new MaterialDistribution();
    d.setMaterialId(item.getId());
    members.findByUserId(request.getRequestedBy()).ifPresent(m -> d.setMemberId(m.getId()));
    d.setQuantity(request.getQuantity());
    if(reviewer!=null) d.setDistributedBy(reviewer.getId());
    distributions.save(d);
  }
  private void notifyRequester(MaterialRequest request, String message){
    if(request.getRequestedBy()==null) return;
    Notification n=new Notification();
    n.setUserId(request.getRequestedBy());
    n.setMessage(message);
    n.setType("MATERIAL");
    notifications.save(n);
  }
}
