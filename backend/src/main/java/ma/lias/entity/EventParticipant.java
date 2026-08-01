package ma.lias.entity;
import jakarta.persistence.*;
@Entity @Table(name="event_participants")
public class EventParticipant { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(name="event_id") private Long eventId; @Column(name="member_id") private Long memberId; public Long getId(){return id;} public void setId(Long id){this.id=id;} public Long getEventId(){return eventId;} public void setEventId(Long eventId){this.eventId=eventId;} public Long getMemberId(){return memberId;} public void setMemberId(Long memberId){this.memberId=memberId;} }
