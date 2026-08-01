package ma.lias.controller;

import ma.lias.entity.MembershipRequestStatus;
import ma.lias.repository.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/dashboard")
public class DashboardController {
  private final MemberRepository members; private final EventRepository events; private final PublicationRepository publications; private final DocumentRecordRepository documents; private final MembershipRequestRepository requests; private final AuditLogRepository audit;
  public DashboardController(MemberRepository members, EventRepository events, PublicationRepository publications, DocumentRecordRepository documents, MembershipRequestRepository requests, AuditLogRepository audit){this.members=members;this.events=events;this.publications=publications;this.documents=documents;this.requests=requests;this.audit=audit;}
  @GetMapping public Map<String,Object> dashboard(){ return Map.of("stats",Map.of("members",members.count(),"events",events.count(),"publications",publications.count(),"documents",documents.count(),"pendingRequests",requests.findByStatus(MembershipRequestStatus.PENDING).size()),"events",events.findTop10ByOrderByStartDateDesc(),"publications",publications.findTop10ByOrderByYearDescCreatedAtDesc(),"audit",audit.findAll().stream().sorted((a,b)->b.getTimestamp().compareTo(a.getTimestamp())).limit(10).toList()); }
}
