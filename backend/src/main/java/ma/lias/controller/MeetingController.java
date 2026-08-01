package ma.lias.controller;

import ma.lias.entity.*;
import ma.lias.repository.*;
import ma.lias.service.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;

@RestController @RequestMapping("/api/meetings")
public class MeetingController {
  private final MeetingRepository repo; private final DocumentRecordRepository documents; private final NotificationRepository notifications; private final UserRepository users; private final AuditService audit; private final FileStorageService storage;
  public MeetingController(MeetingRepository repo, DocumentRecordRepository documents, NotificationRepository notifications, UserRepository users, AuditService audit, FileStorageService storage){this.repo=repo;this.documents=documents;this.notifications=notifications;this.users=users;this.audit=audit;this.storage=storage;}
  @GetMapping public List<Meeting> all(){ return repo.findAll(); }
  @GetMapping("/{id}") public Meeting one(@PathVariable Long id){ return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Introuvable")); }
  @PostMapping public ResponseEntity<Meeting> create(@RequestBody Meeting body){ Meeting saved=repo.save(body); audit.log("CREATE","Meeting", saved.getId(), "Création"); return ResponseEntity.status(HttpStatus.CREATED).body(saved); }
  @PutMapping("/{id}") public Meeting update(@PathVariable Long id, @RequestBody Meeting body){ if(!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Introuvable"); body.setId(id); Meeting saved=repo.save(body); audit.log("UPDATE","Meeting", id, "Modification"); return saved; }
  @PostMapping("/{id}/pv") public Meeting uploadPv(@PathVariable Long id, @RequestParam MultipartFile file, Authentication auth) throws Exception { Meeting m=one(id); m.setPvUrl(storage.save(file,"pv")); Meeting saved=repo.save(m); DocumentRecord d=new DocumentRecord(); d.setFilename(file.getOriginalFilename()); d.setType(DocumentType.PV); d.setFileUrl(m.getPvUrl()); d.setDescription("PV réunion #"+id); if(auth!=null && auth.getPrincipal() instanceof User u) d.setUploadedBy(u.getId()); documents.save(d); notifyActiveUsers("PV archivé: "+m.getTitle(),"PV"); audit.log("UPLOAD","Meeting",id,"PV ajouté et intégré à l'archive documentaire"); return saved; }
  @DeleteMapping("/{id}") public ResponseEntity<?> delete(@PathVariable Long id){ Meeting m=one(id); m.setTitle("[ARCHIVE] "+m.getTitle()); repo.save(m); audit.log("ARCHIVE","Meeting", id, "Réunion archivée sans suppression destructive"); return ResponseEntity.noContent().build(); }
  private void notifyActiveUsers(String message, String type){ users.findAll().stream().filter(u -> u.getStatus()==UserStatus.ACTIVE).forEach(u -> { Notification n=new Notification(); n.setUserId(u.getId()); n.setMessage(message); n.setType(type); notifications.save(n); }); }
}
