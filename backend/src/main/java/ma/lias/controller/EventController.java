package ma.lias.controller;

import ma.lias.entity.*;
import ma.lias.repository.*;
import ma.lias.service.AuditService;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController @RequestMapping("/api/events")
public class EventController {
  private final EventRepository repo; private final DocumentRecordRepository documents; private final MessageRepository messages; private final EventParticipantRepository participants; private final MemberRepository members; private final UserRepository users; private final NotificationRepository notifications; private final AuditService audit;
  public EventController(EventRepository repo, DocumentRecordRepository documents, MessageRepository messages, EventParticipantRepository participants, MemberRepository members, UserRepository users, NotificationRepository notifications, AuditService audit){this.repo=repo;this.documents=documents;this.messages=messages;this.participants=participants;this.members=members;this.users=users;this.notifications=notifications;this.audit=audit;}
  @GetMapping public List<Event> all(){ return repo.findAll(); }
  @GetMapping("/{id}") public Map<String,Object> one(@PathVariable Long id){ Event event=event(id); Map<String,Object> out=new LinkedHashMap<>(); out.put("event", event); out.put("documents", documents.findByEventId(id).stream().filter(d -> !d.isArchived()).toList()); out.put("messages", messages.findByEventIdAndMessageTypeAndDeletedAtIsNullOrderBySentAtAsc(id,MessageType.EVENT)); out.put("participants",participants.findAll().stream().filter(p -> Objects.equals(p.getEventId(),id)).toList()); return out; }
  @PostMapping("/{id}/participants/{memberId}") public ResponseEntity<EventParticipant> addParticipant(@PathVariable Long id,@PathVariable Long memberId){ event(id); if(!members.existsById(memberId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Membre introuvable"); boolean exists=participants.findAll().stream().anyMatch(p -> Objects.equals(p.getEventId(),id)&&Objects.equals(p.getMemberId(),memberId)); if(exists) throw new ResponseStatusException(HttpStatus.CONFLICT,"Ce membre participe déjà à l'événement"); EventParticipant p=new EventParticipant();p.setEventId(id);p.setMemberId(memberId);EventParticipant saved=participants.save(p);audit.log("ADD_PARTICIPANT","Event",id,"Membre #"+memberId);return ResponseEntity.status(HttpStatus.CREATED).body(saved); }
  @DeleteMapping("/{id}/participants/{memberId}") public ResponseEntity<?> removeParticipant(@PathVariable Long id,@PathVariable Long memberId){ EventParticipant participant=participants.findAll().stream().filter(p -> Objects.equals(p.getEventId(),id)&&Objects.equals(p.getMemberId(),memberId)).findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Participation introuvable"));participants.delete(participant);audit.log("REMOVE_PARTICIPANT","Event",id,"Membre #"+memberId);return ResponseEntity.noContent().build(); }
  @PostMapping public ResponseEntity<Event> create(@RequestBody Event body, Authentication auth){ if(auth!=null && auth.getPrincipal() instanceof User u) members.findByUserId(u.getId()).ifPresent(m -> body.setOrganizerId(m.getId())); Event saved=repo.save(body); notifyActiveUsers("Nouvel événement LIAS: "+saved.getTitle(),"EVENT"); audit.log("CREATE","Event", saved.getId(), "Création"); return ResponseEntity.status(HttpStatus.CREATED).body(saved); }
  @PutMapping("/{id}") public Event update(@PathVariable Long id, @RequestBody Event body){ Event existing=event(id); body.setId(id); if(body.getOrganizerId()==null) body.setOrganizerId(existing.getOrganizerId()); Event saved=repo.save(body); audit.log("UPDATE","Event", id, "Modification"); return saved; }
  @PatchMapping("/{id}/archive") public Event archive(@PathVariable Long id){ Event e=event(id); e.setArchived(true); audit.log("ARCHIVE","Event", id, "Archivage édition/événement"); return repo.save(e); }
  @DeleteMapping("/{id}") public ResponseEntity<?> delete(@PathVariable Long id){ Event e=event(id); e.setArchived(true); repo.save(e); audit.log("ARCHIVE","Event", id, "Événement archivé sans suppression destructive"); return ResponseEntity.noContent().build(); }
  private Event event(Long id){ return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Introuvable")); }
  private void notifyActiveUsers(String message, String type){ users.findAll().stream().filter(u -> u.getStatus()==UserStatus.ACTIVE).forEach(u -> { Notification n=new Notification(); n.setUserId(u.getId()); n.setMessage(message); n.setType(type); notifications.save(n); }); }
}
