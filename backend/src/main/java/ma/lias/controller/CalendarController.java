package ma.lias.controller;

import ma.lias.entity.*;
import ma.lias.repository.*;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {
  private final EventRepository events;
  private final MeetingRepository meetings;
  private final MandateRepository mandates;
  private final ConventionRepository conventions;

  public CalendarController(
      EventRepository events,
      MeetingRepository meetings,
      MandateRepository mandates,
      ConventionRepository conventions) {
    this.events = events;
    this.meetings = meetings;
    this.mandates = mandates;
    this.conventions = conventions;
  }

  @GetMapping
  public Map<String, Object> calendar(
      @RequestParam(required = false) LocalDate from,
      @RequestParam(required = false) LocalDate to) {
    LocalDate first = from == null ? LocalDate.now().withDayOfMonth(1) : from;
    LocalDate last = to == null ? first.plusMonths(1).minusDays(1) : to;
    if (last.isBefore(first)) throw new IllegalArgumentException("La date de fin doit suivre la date de début.");

    List<Map<String, Object>> items = new ArrayList<>();
    events.findAll().stream()
      .filter(e -> e.getStartDate() != null && overlaps(e.getStartDate().toLocalDate(), date(e.getEndDate()), first, last))
      .forEach(e -> items.add(item("EVENT", e.getId(), e.getTitle(), e.getStartDate(), e.getEndDate(), e.getLocation(), e.getType() == null ? null : e.getType().name(), "/evenements/" + e.getId())));
    meetings.findAll().stream()
      .filter(m -> m.getDate() != null && between(m.getDate().toLocalDate(), first, last))
      .forEach(m -> items.add(item("MEETING", m.getId(), m.getTitle(), m.getDate(), m.getDate(), null, "Réunion", "/reunions/" + m.getId())));
    mandates.findAll().stream()
      .filter(m -> m.getStartDate() != null && overlaps(m.getStartDate(), m.getEndDate(), first, last))
      .forEach(m -> items.add(item("MANDATE", m.getId(), "Mandat de direction", atStart(m.getStartDate()), atEnd(m.getEndDate()), null, "Gouvernance", "/admin/gouvernance")));
    conventions.findAll().stream()
      .filter(c -> c.getStartDate() != null && overlaps(c.getStartDate(), c.getEndDate(), first, last))
      .forEach(c -> items.add(item("CONVENTION", c.getId(), c.getPartnerName(), atStart(c.getStartDate()), atEnd(c.getEndDate()), c.getPartnerCountry(), "Partenariat", "/conventions/" + c.getId())));

    items.sort(Comparator.comparing(value -> (LocalDateTime) value.get("start")));
    Map<String, Long> summary = new LinkedHashMap<>();
    for (String type : List.of("EVENT", "MEETING", "MANDATE", "CONVENTION")) {
      summary.put(type, items.stream().filter(item -> type.equals(item.get("type"))).count());
    }
    return Map.of("from", first, "to", last, "items", items, "summary", summary);
  }

  private Map<String, Object> item(String type, Long id, String title, LocalDateTime start, LocalDateTime end, String location, String category, String url) {
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("type", type);
    item.put("id", id);
    item.put("title", title);
    item.put("start", start);
    item.put("end", end);
    item.put("location", location);
    item.put("category", category);
    item.put("url", url);
    return item;
  }

  private boolean between(LocalDate value, LocalDate from, LocalDate to) {
    return !value.isBefore(from) && !value.isAfter(to);
  }

  private boolean overlaps(LocalDate start, LocalDate end, LocalDate from, LocalDate to) {
    LocalDate effectiveEnd = end == null ? LocalDate.MAX : end;
    return !effectiveEnd.isBefore(from) && !start.isAfter(to);
  }

  private LocalDate date(LocalDateTime value) { return value == null ? null : value.toLocalDate(); }
  private LocalDateTime atStart(LocalDate value) { return value == null ? null : value.atStartOfDay(); }
  private LocalDateTime atEnd(LocalDate value) { return value == null ? null : value.atTime(23, 59, 59); }
}
