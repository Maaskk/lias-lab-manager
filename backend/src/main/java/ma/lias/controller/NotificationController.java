package ma.lias.controller;

import ma.lias.entity.*;
import ma.lias.repository.NotificationRepository;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
  private final NotificationRepository notifications;

  public NotificationController(NotificationRepository notifications) {
    this.notifications = notifications;
  }

  @GetMapping
  public List<Notification> all(Authentication auth) {
    User actor = actor(auth);
    return notifications.findByUserIdOrderByCreatedAtDesc(actor.getId());
  }

  @GetMapping("/unread-count")
  public Map<String, Long> unreadCount(Authentication auth) {
    User actor = actor(auth);
    return Map.of("count", notifications.countByUserIdAndReadFalse(actor.getId()));
  }

  @PatchMapping("/{id}/read")
  public Notification read(@PathVariable Long id, Authentication auth) {
    User actor = actor(auth);
    Notification notification = notifications.findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification introuvable"));
    if (!Objects.equals(notification.getUserId(), actor.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Notification non autorisée");
    }
    notification.setRead(true);
    return notifications.save(notification);
  }

  @PatchMapping("/read-all")
  public List<Notification> readAll(Authentication auth) {
    List<Notification> list = all(auth);
    list.forEach(notification -> notification.setRead(true));
    return notifications.saveAll(list);
  }

  private User actor(Authentication auth) {
    if (auth != null && auth.getPrincipal() instanceof User user) return user;
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentification requise");
  }
}
