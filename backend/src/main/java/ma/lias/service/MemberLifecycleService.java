package ma.lias.service;

import ma.lias.entity.*;
import ma.lias.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Objects;

@Service
public class MemberLifecycleService {
  private static final Long DEFAULT_LAB_ID = 1L;

  private final MemberRepository members;
  private final UserRepository users;
  private final AffiliationRepository affiliations;
  private final AuditService audit;

  public MemberLifecycleService(MemberRepository members, UserRepository users, AffiliationRepository affiliations, AuditService audit) {
    this.members = members;
    this.users = users;
    this.affiliations = affiliations;
    this.audit = audit;
  }

  public Member retire(Long memberId) {
    Member member = loadMember(memberId);
    MemberType previous = member.getType();
    member.setType(MemberType.RETIRED);
    syncUserStatus(member, UserStatus.FROZEN);
    closeOpenAffiliation(member.getId(), LocalDate.now());
    Member saved = members.save(member);
    audit.log("LIFECYCLE_RETIRE", "Member", memberId, "Retraite: " + previous + " -> RETIRED, compte gelé, affiliation clôturée");
    return saved;
  }

  public Member markFormer(Long memberId) {
    Member member = loadMember(memberId);
    MemberType previous = member.getType();
    member.setType(MemberType.FORMER);
    syncUserStatus(member, UserStatus.DISABLED);
    closeOpenAffiliation(member.getId(), LocalDate.now());
    Member saved = members.save(member);
    audit.log("LIFECYCLE_FORMER", "Member", memberId, "Ancien membre: " + previous + " -> FORMER, compte désactivé, affiliation clôturée");
    return saved;
  }

  public Member reactivate(Long memberId, MemberType type) {
    Member member = loadMember(memberId);
    MemberType target = type == null || type == MemberType.RETIRED || type == MemberType.FORMER ? MemberType.PERMANENT : type;
    MemberType previous = member.getType();
    member.setType(target);
    if (member.getAffiliationDate() == null) member.setAffiliationDate(LocalDate.now());
    syncUserStatus(member, UserStatus.ACTIVE);
    openAffiliation(member.getId(), LocalDate.now());
    Member saved = members.save(member);
    audit.log("LIFECYCLE_REACTIVATE", "Member", memberId, "Réactivation: " + previous + " -> " + target + ", compte actif, nouvelle affiliation ouverte");
    return saved;
  }

  public Member changeType(Long memberId, MemberType type) {
    if (type == MemberType.RETIRED) return retire(memberId);
    if (type == MemberType.FORMER) return markFormer(memberId);
    Member member = loadMember(memberId);
    MemberType previous = member.getType();
    member.setType(type);
    if (previous == MemberType.RETIRED || previous == MemberType.FORMER) {
      syncUserStatus(member, UserStatus.ACTIVE);
      openAffiliation(member.getId(), LocalDate.now());
    }
    Member saved = members.save(member);
    audit.log("STATUS_CHANGE", "Member", memberId, "Statut membre: " + previous + " -> " + type);
    return saved;
  }

  public void openInitialAffiliation(Member member) {
    if (member.getAffiliationDate() == null) member.setAffiliationDate(LocalDate.now());
    members.save(member);
    openAffiliation(member.getId(), member.getAffiliationDate());
  }

  private Member loadMember(Long memberId) {
    return members.findById(memberId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membre introuvable"));
  }

  private void syncUserStatus(Member member, UserStatus status) {
    if (member.getUserId() == null) return;
    users.findById(member.getUserId()).ifPresent(user -> {
      user.setStatus(status);
      users.save(user);
    });
  }

  private void closeOpenAffiliation(Long memberId, LocalDate endDate) {
    affiliations.findAll().stream()
      .filter(a -> Objects.equals(a.getMemberId(), memberId) && a.getEndDate() == null)
      .forEach(a -> {
        a.setEndDate(endDate);
        affiliations.save(a);
      });
  }

  private void openAffiliation(Long memberId, LocalDate startDate) {
    boolean alreadyOpen = affiliations.findAll().stream()
      .anyMatch(a -> Objects.equals(a.getMemberId(), memberId) && a.getEndDate() == null);
    if (alreadyOpen) return;
    Affiliation affiliation = new Affiliation();
    affiliation.setMemberId(memberId);
    affiliation.setLabId(DEFAULT_LAB_ID);
    affiliation.setStartDate(startDate == null ? LocalDate.now() : startDate);
    affiliations.save(affiliation);
  }
}
