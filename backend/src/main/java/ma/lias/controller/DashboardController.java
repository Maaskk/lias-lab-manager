package ma.lias.controller;

import ma.lias.entity.MembershipRequestStatus;
import ma.lias.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
  private final MemberRepository members;
  private final EventRepository events;
  private final PublicationRepository publications;
  private final DocumentRecordRepository documents;
  private final MembershipRequestRepository requests;
  private final AuditLogRepository audit;

  public DashboardController(MemberRepository members, EventRepository events, PublicationRepository publications, DocumentRecordRepository documents, MembershipRequestRepository requests, AuditLogRepository audit) {
    this.members = members;
    this.events = events;
    this.publications = publications;
    this.documents = documents;
    this.requests = requests;
    this.audit = audit;
  }

  @GetMapping
  public Map<String, Object> dashboard(Authentication auth) {
    boolean manager = auth != null && auth.getAuthorities().stream()
      .anyMatch(authority -> Set.of("ROLE_ADMIN", "ROLE_DIRECTOR", "ROLE_VICE_DIRECTOR").contains(authority.getAuthority()));

    Map<String, Object> stats = new LinkedHashMap<>();
    stats.put("members", members.count());
    stats.put("events", events.count());
    stats.put("publications", publications.count());
    stats.put("documents", documents.count());
    if (manager) stats.put("pendingRequests", requests.findByStatus(MembershipRequestStatus.PENDING).size());

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("stats", stats);
    response.put("events", events.findTop10ByOrderByStartDateDesc());
    response.put("publications", publications.findTop10ByOrderByYearDescCreatedAtDesc());
    response.put("audit", manager ? audit.findAll().stream().sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())).limit(10).toList() : List.of());
    return response;
  }
}
