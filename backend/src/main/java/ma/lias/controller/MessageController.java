package ma.lias.controller;

import ma.lias.entity.*;
import ma.lias.repository.*;
import ma.lias.service.AuditService;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
  private final MessageRepository messages;
  private final MemberRepository members;
  private final UserRepository users;
  private final TeamRepository teams;
  private final EventRepository events;
  private final AuditService audit;

  public MessageController(
      MessageRepository messages,
      MemberRepository members,
      UserRepository users,
      TeamRepository teams,
      EventRepository events,
      AuditService audit) {
    this.messages = messages;
    this.members = members;
    this.users = users;
    this.teams = teams;
    this.events = events;
    this.audit = audit;
  }

  @GetMapping
  public List<Message> all(
      @RequestParam(required = false) Long receiverId,
      @RequestParam(required = false) Long teamId,
      @RequestParam(required = false) Long eventId,
      Authentication auth) {
    User actor = actor(auth);
    if (receiverId != null) {
      requireActiveUser(receiverId);
      return messages.findDirectConversation(actor.getId(), receiverId);
    }
    if (teamId != null) {
      requireTeamAccess(actor, teamId);
      return messages.findByTeamIdAndMessageTypeAndDeletedAtIsNullOrderBySentAtAsc(teamId, MessageType.TEAM);
    }
    if (eventId != null) {
      requireEvent(eventId);
      return messages.findByEventIdAndMessageTypeAndDeletedAtIsNullOrderBySentAtAsc(eventId, MessageType.EVENT);
    }
    return messages.findTop50ByMessageTypeAndDeletedAtIsNullOrderBySentAtDesc(MessageType.GLOBAL);
  }

  @PostMapping
  public ResponseEntity<Message> create(@RequestBody Message body, Authentication auth) {
    User actor = actor(auth);
    String content = body.getContent() == null ? "" : body.getContent().trim();
    if (content.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le message ne peut pas être vide.");
    }
    if (content.length() > 5000) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le message dépasse 5000 caractères.");
    }

    MessageType type = body.getMessageType() == null ? inferType(body) : body.getMessageType();
    body.setId(null);
    body.setSenderId(actor.getId());
    body.setMessageType(type);
    body.setContent(content);
    body.setSentAt(LocalDateTime.now());
    body.setDeletedAt(null);
    validateAndNormalize(body, actor);

    Message saved = messages.save(body);
    audit.log("CREATE", "Message", saved.getId(), "Canal: " + type);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
    User actor = actor(auth);
    Message message = messages.findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message introuvable"));
    if (!Objects.equals(message.getSenderId(), actor.getId()) && !isManager(auth)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Suppression non autorisée");
    }
    message.setDeletedAt(LocalDateTime.now());
    messages.save(message);
    audit.log("ARCHIVE", "Message", id, "Message masqué sans suppression destructive");
    return ResponseEntity.noContent().build();
  }

  private void validateAndNormalize(Message message, User actor) {
    switch (message.getMessageType()) {
      case DIRECT -> {
        if (message.getReceiverId() == null) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le destinataire est obligatoire.");
        }
        requireActiveUser(message.getReceiverId());
        message.setTeamId(null);
        message.setEventId(null);
      }
      case TEAM -> {
        if (message.getTeamId() == null) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'équipe est obligatoire.");
        }
        requireTeamAccess(actor, message.getTeamId());
        message.setReceiverId(null);
        message.setEventId(null);
      }
      case EVENT -> {
        if (message.getEventId() == null) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'événement est obligatoire.");
        }
        requireEvent(message.getEventId());
        message.setReceiverId(null);
        message.setTeamId(null);
      }
      case GLOBAL -> {
        message.setReceiverId(null);
        message.setTeamId(null);
        message.setEventId(null);
      }
    }
  }

  private MessageType inferType(Message message) {
    if (message.getReceiverId() != null) return MessageType.DIRECT;
    if (message.getTeamId() != null) return MessageType.TEAM;
    if (message.getEventId() != null) return MessageType.EVENT;
    return MessageType.GLOBAL;
  }

  private User actor(Authentication auth) {
    if (auth != null && auth.getPrincipal() instanceof User user) return user;
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentification requise");
  }

  private void requireActiveUser(Long userId) {
    User user = users.findById(userId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destinataire introuvable"));
    if (user.getStatus() != UserStatus.ACTIVE) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Le compte destinataire n'est pas actif.");
    }
  }

  private void requireEvent(Long eventId) {
    if (!events.existsById(eventId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Événement introuvable");
    }
  }

  private void requireTeamAccess(User actor, Long teamId) {
    if (!teams.existsById(teamId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Équipe introuvable");
    }
    if (Set.of(AppRole.ADMIN, AppRole.DIRECTOR, AppRole.VICE_DIRECTOR).contains(actor.getAppRole())) return;
    Member member = members.findByUserId(actor.getId())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Profil membre introuvable"));
    if (!Objects.equals(member.getCurrentTeamId(), teamId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cette discussion est réservée aux membres de l'équipe.");
    }
  }

  private boolean isManager(Authentication auth) {
    return auth != null && auth.getAuthorities().stream()
      .anyMatch(a -> Set.of("ROLE_ADMIN", "ROLE_DIRECTOR", "ROLE_VICE_DIRECTOR").contains(a.getAuthority()));
  }
}
