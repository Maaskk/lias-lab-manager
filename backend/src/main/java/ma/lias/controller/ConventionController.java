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

@RestController @RequestMapping("/api/conventions")
public class ConventionController {
  private final ConventionRepository repo; private final DocumentRecordRepository documents; private final NotificationRepository notifications; private final UserRepository users; private final AuditService audit; private final FileStorageService storage;
  public ConventionController(ConventionRepository repo, DocumentRecordRepository documents, NotificationRepository notifications, UserRepository users, AuditService audit, FileStorageService storage){this.repo=repo;this.documents=documents;this.notifications=notifications;this.users=users;this.audit=audit;this.storage=storage;}
  @GetMapping public List<Convention> all(){ return repo.findAll(); }
  @GetMapping("/{id}") public Convention one(@PathVariable Long id){ return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Introuvable")); }
  @PostMapping public ResponseEntity<Convention> create(@RequestBody Convention body){ Convention saved=repo.save(body); audit.log("CREATE","Convention", saved.getId(), "Création"); return ResponseEntity.status(HttpStatus.CREATED).body(saved); }
  @PutMapping("/{id}") public Convention update(@PathVariable Long id, @RequestBody Convention body){ if(!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Introuvable"); body.setId(id); Convention saved=repo.save(body); audit.log("UPDATE","Convention", id, "Modification"); return saved; }
  @PostMapping("/{id}/document") public Convention uploadDocument(@PathVariable Long id, @RequestParam MultipartFile file, Authentication auth) throws Exception { Convention c=one(id); c.setDocumentUrl(storage.save(file,"conventions")); Convention saved=repo.save(c); DocumentRecord d=new DocumentRecord(); d.setFilename(file.getOriginalFilename()); d.setType(DocumentType.CONVENTION); d.setFileUrl(c.getDocumentUrl()); d.setDescription("Convention #"+id+" - "+c.getPartnerName()); if(auth!=null && auth.getPrincipal() instanceof User u) d.setUploadedBy(u.getId()); documents.save(d); notifyActiveUsers("Document de convention archivé: "+c.getPartnerName(),"CONVENTION"); audit.log("UPLOAD","Convention",id,"Document convention ajouté à l'archive documentaire"); return saved; }
  @DeleteMapping("/{id}") public ResponseEntity<?> delete(@PathVariable Long id){ Convention c=one(id); c.setDescription((c.getDescription()==null?"":c.getDescription()+"\n")+"[ARCHIVE] Convention archivée."); repo.save(c); audit.log("ARCHIVE","Convention", id, "Convention archivée sans suppression destructive"); return ResponseEntity.noContent().build(); }
  private void notifyActiveUsers(String message, String type){ users.findAll().stream().filter(u -> u.getStatus()==UserStatus.ACTIVE).forEach(u -> { Notification n=new Notification(); n.setUserId(u.getId()); n.setMessage(message); n.setType(type); notifications.save(n); }); }
}
