package ma.lias.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "sender_id")
  private Long senderId;

  @Column(name = "receiver_id")
  private Long receiverId;

  @Column(name = "team_id")
  private Long teamId;

  @Column(name = "event_id")
  private Long eventId;

  @Enumerated(EnumType.STRING)
  @Column(name = "message_type", nullable = false)
  private MessageType messageType = MessageType.GLOBAL;

  @Column(length = 5000, nullable = false)
  private String content;

  @Column(name = "sent_at", nullable = false)
  private LocalDateTime sentAt = LocalDateTime.now();

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getSenderId() { return senderId; }
  public void setSenderId(Long senderId) { this.senderId = senderId; }
  public Long getReceiverId() { return receiverId; }
  public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
  public Long getTeamId() { return teamId; }
  public void setTeamId(Long teamId) { this.teamId = teamId; }
  public Long getEventId() { return eventId; }
  public void setEventId(Long eventId) { this.eventId = eventId; }
  public MessageType getMessageType() { return messageType; }
  public void setMessageType(MessageType messageType) { this.messageType = messageType; }
  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
  public LocalDateTime getSentAt() { return sentAt; }
  public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
  public LocalDateTime getDeletedAt() { return deletedAt; }
  public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
