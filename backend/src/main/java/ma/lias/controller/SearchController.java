package ma.lias.controller;

import ma.lias.entity.*;
import ma.lias.repository.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {
  private final MemberRepository members;
  private final DocumentRecordRepository documents;
  private final EventRepository events;
  private final PublicationRepository publications;

  public SearchController(
      MemberRepository members,
      DocumentRecordRepository documents,
      EventRepository events,
      PublicationRepository publications) {
    this.members = members;
    this.documents = documents;
    this.events = events;
    this.publications = publications;
  }

  @GetMapping
  public Map<String, Object> search(
      @RequestParam(defaultValue="") String q,
      @RequestParam(defaultValue="0") int page,
      @RequestParam(defaultValue="20") int limit) {
    String needle = q.toLowerCase(Locale.ROOT).trim();
    int safePage = Math.max(0, page);
    int safeLimit = Math.max(1, Math.min(limit, 50));

    List<Map<String, Object>> memberResults = needle.length() < 2 ? List.of() : members.findAll().stream()
      .filter(m -> contains(needle, m.getFirstName(), m.getLastName(), m.getInterests(), m.getBiography(), m.getEstablishment(), m.getOriginLab()))
      .map(this::safeMember)
      .toList();
    List<DocumentRecord> documentResults = needle.length() < 2 ? List.of() : documents.findAll().stream()
      .filter(d -> !d.isArchived())
      .filter(d -> contains(needle, d.getFilename(), d.getDescription(), d.getType() == null ? null : d.getType().name()))
      .toList();
    List<Event> eventResults = needle.length() < 2 ? List.of() : events.findAll().stream()
      .filter(e -> contains(needle, e.getTitle(), e.getDescription(), e.getLocation(), e.getEdition(), e.getType() == null ? null : e.getType().name()))
      .toList();
    List<Publication> publicationResults = needle.length() < 2 ? List.of() : publications.findAll().stream()
      .filter(p -> contains(needle, p.getTitle(), p.getAuthors(), p.getAbstractText(), p.getDoi(), p.getPublicationUrl(), String.valueOf(p.getYear())))
      .toList();

    Map<String, Integer> totals = new LinkedHashMap<>();
    totals.put("members", memberResults.size());
    totals.put("documents", documentResults.size());
    totals.put("events", eventResults.size());
    totals.put("publications", publicationResults.size());
    int total = totals.values().stream().mapToInt(Integer::intValue).sum();

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("query", q);
    response.put("page", safePage);
    response.put("limit", safeLimit);
    response.put("total", total);
    response.put("totals", totals);
    response.put("hasNext", totals.values().stream().anyMatch(count -> (safePage + 1) * safeLimit < count));
    response.put("members", paginate(memberResults, safePage, safeLimit));
    response.put("documents", paginate(documentResults, safePage, safeLimit));
    response.put("events", paginate(eventResults, safePage, safeLimit));
    response.put("publications", paginate(publicationResults, safePage, safeLimit));
    return response;
  }

  private <T> List<T> paginate(List<T> values, int page, int limit) {
    int start = Math.min(page * limit, values.size());
    int end = Math.min(start + limit, values.size());
    return values.subList(start, end);
  }

  private boolean contains(String needle, String... values) {
    for (String value : values) {
      if (value != null && value.toLowerCase(Locale.ROOT).contains(needle)) return true;
    }
    return false;
  }

  private Map<String, Object> safeMember(Member member) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("id", member.getId());
    out.put("firstName", member.getFirstName());
    out.put("lastName", member.getLastName());
    out.put("photoUrl", member.getPhotoUrl());
    out.put("type", member.getType());
    out.put("currentTeamId", member.getCurrentTeamId());
    out.put("interests", member.getInterests());
    out.put("establishment", member.getEstablishment());
    return out;
  }
}
