package ma.lias.controller;

import ma.lias.entity.*;
import ma.lias.repository.DocumentRecordRepository;
import ma.lias.service.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;

@RestController @RequestMapping("/api/documents")
public class DocumentController {
  private final DocumentRecordRepository repo; private final FileStorageService storage; private final AuditService audit;
  public DocumentController(DocumentRecordRepository repo, FileStorageService storage, AuditService audit){this.repo=repo;this.storage=storage;this.audit=audit;}
  @GetMapping public List<DocumentRecord> all(@RequestParam(defaultValue="false") boolean includeArchived, @RequestParam(required=false) Long eventId){ return repo.findAll().stream().filter(d -> includeArchived || !d.isArchived()).filter(d -> eventId==null || Objects.equals(d.getEventId(), eventId)).toList(); }
  @GetMapping("/{id}") public DocumentRecord one(@PathVariable Long id){ return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Document introuvable")); }
  @PostMapping("/upload") public ResponseEntity<DocumentRecord> upload(@RequestParam MultipartFile file, @RequestParam DocumentType type, @RequestParam(required=false) Long eventId, @RequestParam(required=false) String description, Authentication auth) throws Exception { DocumentRecord d=new DocumentRecord(); d.setFilename(file.getOriginalFilename()); d.setType(type); d.setEventId(eventId); d.setDescription(description); d.setVersion(nextVersion(d.getFilename(), type, eventId)); d.setFileUrl(storage.save(file,"documents")); if(auth!=null && auth.getPrincipal() instanceof User u) d.setUploadedBy(u.getId()); DocumentRecord saved=repo.save(d); audit.log("UPLOAD","Document",saved.getId(),"Document ajouté par utilisateur authentifié v"+saved.getVersion()); return ResponseEntity.status(HttpStatus.CREATED).body(saved); }
  @DeleteMapping("/{id}") public ResponseEntity<?> delete(@PathVariable Long id){ DocumentRecord d=one(id); d.setArchived(true); d.setArchivedAt(LocalDateTime.now()); repo.save(d); audit.log("ARCHIVE","Document",id,"Document archivé sans suppression destructive"); return ResponseEntity.noContent().build(); }
  private int nextVersion(String filename, DocumentType type, Long eventId){ return repo.findAll().stream().filter(d -> Objects.equals(d.getFilename(), filename) && Objects.equals(d.getType(), type) && Objects.equals(d.getEventId(), eventId)).mapToInt(DocumentRecord::getVersion).max().orElse(0)+1; }
}
