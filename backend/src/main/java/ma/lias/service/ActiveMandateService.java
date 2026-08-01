package ma.lias.service;

import ma.lias.entity.*;
import ma.lias.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;

@Service
public class ActiveMandateService {
  private final MandateRepository mandates;
  private final MemberRepository members;
  private final UserRepository users;
  private final RoleHistoryRepository roleHistory;
  private final AuditService audit;

  public ActiveMandateService(MandateRepository mandates, MemberRepository members, UserRepository users, RoleHistoryRepository roleHistory, AuditService audit) {
    this.mandates = mandates;
    this.members = members;
    this.users = users;
    this.roleHistory = roleHistory;
    this.audit = audit;
  }

  public Optional<Mandate> activeMandate() {
    LocalDate today = LocalDate.now();
    return mandates.findAll().stream()
      .filter(m -> startsBeforeOrToday(m, today) && endsAfterOrOpen(m, today))
      .max(Comparator.comparing(Mandate::getStartDate, Comparator.nullsLast(Comparator.naturalOrder())));
  }

  public boolean isActiveDirector(User user) {
    if (user == null) return false;
    Optional<Member> member = members.findByUserId(user.getId());
    Optional<Mandate> active = activeMandate();
    return member.isPresent() && active.isPresent() && Objects.equals(active.get().getDirectorId(), member.get().getId());
  }

  public void requireActiveDirectorOrAdmin(User user) {
    if (user != null && user.getAppRole() == AppRole.ADMIN) return;
    if (!isActiveDirector(user)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Décision réservée au directeur du mandat actif.");
  }

  public void validateNoOverlap(Mandate candidate, Long ignoredId) {
    LocalDate start = candidate.getStartDate();
    LocalDate end = candidate.getEndDate();
    if (start == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date de début du mandat est obligatoire.");
    if (end != null && end.isBefore(start)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date de fin ne peut pas précéder la date de début.");
    boolean overlaps = mandates.findAll().stream()
      .filter(existing -> ignoredId == null || !Objects.equals(existing.getId(), ignoredId))
      .anyMatch(existing -> rangesOverlap(start, end, existing.getStartDate(), existing.getEndDate()));
    if (overlaps) throw new ResponseStatusException(HttpStatus.CONFLICT, "Un mandat actif chevauche déjà cette période.");
  }

  public void syncMandateRoles(Mandate mandate) {
    syncMemberRole(mandate.getDirectorId(), AppRole.DIRECTOR, RoleName.DIRECTOR);
    syncMemberRole(mandate.getViceDirectorId(), AppRole.VICE_DIRECTOR, RoleName.VICE_DIR);
  }

  private void syncMemberRole(Long memberId, AppRole appRole, RoleName roleName) {
    if (memberId == null) return;
    members.findById(memberId).ifPresent(member -> {
      if (member.getUserId() != null) {
        users.findById(member.getUserId()).ifPresent(user -> {
          AppRole before = user.getAppRole();
          user.setAppRole(appRole);
          users.save(user);
          audit.log("ROLE_SYNC", "User", user.getId(), "Mandat actif: " + before + " -> " + appRole);
        });
      }
      LocalDate today = LocalDate.now();
      roleHistory.findAll().stream()
        .filter(r -> Objects.equals(r.getMemberId(), memberId) && r.getEndDate() == null)
        .forEach(r -> { r.setEndDate(today); roleHistory.save(r); });
      RoleHistory next = new RoleHistory();
      next.setMemberId(memberId);
      next.setRole(roleName);
      next.setStartDate(today);
      roleHistory.save(next);
    });
  }

  private boolean startsBeforeOrToday(Mandate mandate, LocalDate date) {
    return mandate.getStartDate() != null && !mandate.getStartDate().isAfter(date);
  }

  private boolean endsAfterOrOpen(Mandate mandate, LocalDate date) {
    return mandate.getEndDate() == null || !mandate.getEndDate().isBefore(date);
  }

  private boolean rangesOverlap(LocalDate aStart, LocalDate aEnd, LocalDate bStart, LocalDate bEnd) {
    if (bStart == null) return false;
    LocalDate leftEnd = aEnd == null ? LocalDate.MAX : aEnd;
    LocalDate rightEnd = bEnd == null ? LocalDate.MAX : bEnd;
    return !leftEnd.isBefore(bStart) && !rightEnd.isBefore(aStart);
  }
}
