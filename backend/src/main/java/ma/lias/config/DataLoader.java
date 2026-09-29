package ma.lias.config;

import ma.lias.entity.*;
import ma.lias.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.*;
import java.util.*;

@Configuration
public class DataLoader {
  private static final String ESTABLISHMENT = "Faculté des Sciences Ben M’Sick — Université Hassan II de Casablanca";
  private static final String ORIGIN_LAB = "Laboratoire d’Intelligence Artificielle et Systèmes (LIAS)";

  @Bean CommandLineRunner seed(
      UserRepository users,
      MemberRepository members,
      TeamRepository teams,
      AffiliationRepository affiliations,
      MandateRepository mandates,
      TeamChiefRepository teamChiefs,
      RoleHistoryRepository roles,
      EventRepository events,
      PublicationRepository pubs,
      ConventionRepository conventions,
      DocumentRecordRepository documents,
      MessageRepository messages,
      MaterialInventoryRepository materialInventory,
      MaterialRequestRepository materialRequests,
      MeetingRepository meetings,
      MembershipRequestRepository membershipRequests,
      AuditLogRepository audit,
      SystemSettingRepository settings,
      PasswordEncoder encoder,
      PlatformTransactionManager transactionManager,
      @Value("${app.seed.initial-password:}") String initialPassword) {
    return args -> new TransactionTemplate(transactionManager).executeWithoutResult(transaction -> {
      if (users.count() > 0) return;
      String seedPassword = requireInitialPassword(initialPassword);

      saveSetting(settings, "lab_name", ORIGIN_LAB);
      saveSetting(settings, "lab_subtitle", "IA • Données • Smart Cities");
      saveSetting(settings, "lab_establishment", ESTABLISHMENT);
      saveSetting(settings, "lab_address", "Faculté des Sciences Ben M’Sick (FSBM), Université Hassan II de Casablanca, B.P. 7955 Sidi‑Othmane, Casablanca, Maroc");
      saveSetting(settings, "lab_contact_email", "lias.fsbm@gmail.com");
      saveSetting(settings, "lab_contact_phone", "(+212) 6 61 44 24 27");
      saveSetting(settings, "lab_website", "https://lias.ma/");
      saveSetting(settings, "lab_logo_url", "/assets/Logo-LIAS-01.png");
      saveSetting(settings, "lab_creation_date", "");
      saveSetting(settings, "lab_public_description", "Le LIAS mène des recherches fondamentales et appliquées en IA, science des données, cloud, IoT, big data et modélisation mathématique pour répondre aux enjeux des villes intelligentes : santé, éducation, transport, agriculture, finance et cybersécurité. Aligné sur Digital Morocco 2030, le laboratoire vise l’excellence scientifique et l’impact socio-économique par l’innovation et les partenariats publics-privés.");
      saveSetting(settings, "public_stats", "4 équipes permanentes;42 thèses en cours (2021–2024);28 thèses soutenues (2021–2024);230+ publications indexées (2021–2024);8 projets de recherche en cours");
      saveSetting(settings, "source_note", "Données publiques extraites des pages officielles lias.ma, lias.ma/icais et lias.ma/icisct. Les comptes applicatifs locaux servent uniquement à tester l’authentification.");

      Team isdiac = teams.save(team("ISDIAC", "IA, Cloud, NLP, sciences de donnée, cloud computing et traitement de signal et d’images."));
      Team sima = teams.save(team("SIMA", "Systèmes intelligents, modélisation avancée, technologies cognitives et intelligence artificielle."));
      Team sdtic = teams.save(team("SDTIC", "Technologie intelligente, architecture IoT et mathématiques appliquées."));
      Team ilias = teams.save(team("ILIAS", "Ingénierie logicielle, architectures de données, informatique et modélisation mathématique."));

      User admin = users.save(user("admin@lias.ma", seedPassword, AppRole.ADMIN, encoder));
      User directorUser = users.save(user("faouzia.benabbou@lias.local", seedPassword, AppRole.DIRECTOR, encoder));
      User viceUser = users.save(user("abdessamad.belangour@lias.local", seedPassword, AppRole.VICE_DIRECTOR, encoder));
      User chiefIsdiacUser = users.save(user("nawal.sael@lias.local", seedPassword, AppRole.TEAM_CHIEF, encoder));
      User chiefSimaUser = users.save(user("mohammed.ait.daoud@lias.local", seedPassword, AppRole.TEAM_CHIEF, encoder));
      User chiefSdticUser = users.save(user("sara.ouahabi@lias.local", seedPassword, AppRole.TEAM_CHIEF, encoder));
      User chiefIliasUser = users.save(user("abdelaziz.ettaoufik@lias.local", seedPassword, AppRole.TEAM_CHIEF, encoder));
      User permanentDemoUser = users.save(user("permanent.demo@lias.local", seedPassword, AppRole.PERMANENT_MEMBER, encoder));
      User associateDemoUser = users.save(user("associe.demo@lias.local", seedPassword, AppRole.ASSOCIATE_MEMBER, encoder));
      User doctoralDemoUser = users.save(user("doctorant.demo@lias.local", seedPassword, AppRole.DOCTORAL, encoder));

      Member director = members.save(member(directorUser, "Faouzia", "Benabbou", MemberType.PERMANENT, isdiac.getId(), "IA, Cloud, NLP", "Directrice du laboratoire"));
      Member vice = members.save(member(viceUser, "Abdessamad", "Belangour", MemberType.PERMANENT, ilias.getId(), "Ingénierie logicielle, architectures de données", "Directeur adjoint"));
      Member chiefIsdiac = members.save(member(chiefIsdiacUser, "Nawal", "Sael", MemberType.PERMANENT, isdiac.getId(), "Sciences de donnée", "Cheffe d’équipe ISDIAC"));
      Member chiefSima = members.save(member(chiefSimaUser, "Mohammed", "Ait Daoud", MemberType.PERMANENT, sima.getId(), "Systèmes intelligents et modélisation avancée", "Chef d’équipe SIMA"));
      Member chiefSdtic = members.save(member(chiefSdticUser, "Sara", "Ouahabi", MemberType.PERMANENT, sdtic.getId(), "Technologie intelligente", "Cheffe d’équipe SDTIC"));
      Member chiefIlias = members.save(member(chiefIliasUser, "Abdelaziz", "Ettaoufik", MemberType.PERMANENT, ilias.getId(), "IA et systèmes", "Chef d’équipe ILIAS"));
      Member permanentDemo = members.save(member(permanentDemoUser, "Amine", "El Idrissi", MemberType.PERMANENT, isdiac.getId(), "Apprentissage automatique et NLP", "Membre permanent utilisé pour démontrer les workflows internes."));
      Member associateDemo = members.save(member(associateDemoUser, "Salma", "Alaoui", MemberType.ASSOCIATE, sima.getId(), "Systèmes intelligents", "Membre associée utilisée pour vérifier les droits de consultation."));
      Member doctoralDemo = members.save(member(doctoralDemoUser, "Yasmine", "Bennis", MemberType.DOCTORAL, isdiac.getId(), "IA générative et recherche d'information", "Doctorante utilisée pour démontrer le profil et le dépôt de publications."));

      List<Member> publicMembers = new ArrayList<>(List.of(director, vice, chiefIsdiac, chiefSima, chiefSdtic, chiefIlias));
      publicMembers.add(savePublicMember(users, members, encoder, seedPassword, "Amal", "Zaouch", isdiac, "Cloud Computing"));
      publicMembers.add(savePublicMember(users, members, encoder, seedPassword, "Fouzia", "Elazzaby", isdiac, "Traitement de signal et d’images"));
      publicMembers.add(savePublicMember(users, members, encoder, seedPassword, "Khadija", "Achtaich", sima, "Technologies Cognitives"));
      publicMembers.add(savePublicMember(users, members, encoder, seedPassword, "Mostafa", "Hanoune", sima, "Systèmes Intelligents"));
      publicMembers.add(savePublicMember(users, members, encoder, seedPassword, "Nabil", "Aharrane", sima, "Intelligence Artificielle"));
      publicMembers.add(savePublicMember(users, members, encoder, seedPassword, "Sanaa", "El Filali", sdtic, "Technologie intelligente"));
      publicMembers.add(savePublicMember(users, members, encoder, seedPassword, "Rachida", "Ait Abdelouahid", sdtic, "Architecture IoT"));
      publicMembers.add(savePublicMember(users, members, encoder, seedPassword, "Naceur", "Achtaich", sdtic, "Mathématiques appliquées"));
      publicMembers.add(savePublicMember(users, members, encoder, seedPassword, "Youssef", "Sekhara", ilias, "Informatique"));
      publicMembers.add(savePublicMember(users, members, encoder, seedPassword, "Driss", "Bouggar", ilias, "Modélisation mathématique"));
      publicMembers.add(permanentDemo);
      publicMembers.add(associateDemo);
      publicMembers.add(doctoralDemo);

      for (Member m : publicMembers) {
        Affiliation a = new Affiliation();
        a.setMemberId(m.getId());
        a.setLabId(1L);
        a.setStartDate(m.getAffiliationDate());
        affiliations.save(a);
        roles.save(role(m, RoleName.MEMBER, null, null));
      }

      Mandate current = mandates.save(mandate(director, vice, LocalDate.of(2025, 1, 1), null));
      teamChiefs.saveAll(List.of(
          chief(current, chiefIsdiac, isdiac),
          chief(current, chiefSima, sima),
          chief(current, chiefSdtic, sdtic),
          chief(current, chiefIlias, ilias)
      ));
      roles.saveAll(List.of(
          role(director, RoleName.DIRECTOR, LocalDate.of(2025, 1, 1), null),
          role(vice, RoleName.VICE_DIR, LocalDate.of(2025, 1, 1), null),
          role(chiefIsdiac, RoleName.TEAM_CHIEF, LocalDate.of(2025, 1, 1), null),
          role(chiefSima, RoleName.TEAM_CHIEF, LocalDate.of(2025, 1, 1), null),
          role(chiefSdtic, RoleName.TEAM_CHIEF, LocalDate.of(2025, 1, 1), null),
          role(chiefIlias, RoleName.TEAM_CHIEF, LocalDate.of(2025, 1, 1), null)
      ));

      pubs.saveAll(List.of(
          publication("Artificial Intelligence for Quality of Life Study", "Jannani A., Sael N., Benabbou F.", 2024, isdiac, chiefIsdiac, "IEEE Access"),
          publication("Hybrid Deep Learning for Bot Detection", "Ellaky Z., Benabbou F.", 2024, isdiac, director, "IEEE Access"),
          publication("Breast Cancer Therapy Prediction", "El Filali S.E. et al.", 2024, sdtic, chiefSdtic, "iJOE"),
          publication("Two-Stage Object Detection Models", "Bouraya S., Belangour A.", 2024, ilias, vice, "BEEI"),
          publication("Secure and Intelligent Software Engineering: Automatic Detection and Correction of Code Vulnerabilities Using Generative AI Agents", "K. Letrach, K. El Bouchti, N. Amimar, S. Ziti", 2025, ilias, vice, "ICAIS’25"),
          publication("Enhancing Smart Contract Security through Transformer-Based AI Models", "N. El Inani, F. Benabbou, S. Ouahabi, K. Sabiri", 2025, sdtic, chiefSdtic, "ICAIS’25"),
          publication("Sentiment Analysis for Arabic Dialects: An Experimental Study", "A. Sebbar, O. Zahour, E. Benlahmar, F.I Mountassir, T. Amzil", 2025, sima, chiefSima, "ICAIS’25"),
          publication("RAG-based Intelligent Agent for Automated Retrieval and Document-Level Summarization of Scientific Publications", "I. Bouboul, R. Ait Abdelouahid", 2025, sdtic, chiefSdtic, "ICAIS’25")
      ));

      Event icais = events.save(event("Organisation de la Conférence ICAIS", EventType.CONFERENCE,
          "International Conference on Artificial Intelligence and Systems (ICAIS), thème : Bridging Theory and Applications for Innovation, Sustainability and Societal Challenges.",
          LocalDateTime.of(2025, 11, 27, 8, 30), LocalDateTime.of(2025, 11, 28, 18, 0), ESTABLISHMENT, director, "ICAIS’25", false));
      events.save(event("Journée Doctorale", EventType.SEMINAR,
          "Recherche scientifique, Intelligence artificielle et Innovation.",
          LocalDateTime.of(2025, 7, 3, 9, 0), LocalDateTime.of(2025, 7, 3, 17, 0), ESTABLISHMENT, vice, "2025", true));
      Event icisct = events.save(event("ICISCT 2026 — Innovative Smart City Technologies", EventType.CONFERENCE,
          "International Conference on Innovative Smart City Technologies, hosted by the Faculty of Sciences Ben M’Sik in Casablanca.",
          LocalDateTime.of(2026, 6, 25, 9, 0), LocalDateTime.of(2026, 6, 27, 18, 0), "FSBM, Casablanca, Morocco", director, "ICISCT’26", false));

      meetings.save(meeting("Réunion de préparation ICAIS", LocalDateTime.of(2025, 10, 15, 14, 0),
          "Répartition des responsabilités, programme scientifique et logistique.", director));

      messages.saveAll(List.of(
          message(directorUser, null, null, null, MessageType.GLOBAL, "Bienvenue dans l'espace de communication interne du LIAS."),
          message(permanentDemoUser, directorUser, null, null, MessageType.DIRECT, "Bonjour Professeure, le compte rendu de l'équipe ISDIAC est prêt."),
          message(directorUser, permanentDemoUser, null, null, MessageType.DIRECT, "Merci, vous pouvez l'ajouter aux documents de l'événement."),
          message(chiefIsdiacUser, null, isdiac, null, MessageType.TEAM, "Réunion d'équipe jeudi à 14h pour suivre les travaux en cours."),
          message(directorUser, null, null, icais, MessageType.EVENT, "Le programme ICAIS est disponible dans les documents de cette édition.")
      ));

      materialInventory.saveAll(List.of(
          material("Ordinateur portable", "Poste de calcul mobile pour les activités de recherche.", 6, "Dotation FSBM"),
          material("Kit IoT", "Capteurs et microcontrôleurs pour les prototypes Smart City.", 10, "Projet SDTIC"),
          material("GPU de calcul", "Accélérateur dédié aux expérimentations d'apprentissage profond.", 2, "Projet ISDIAC")
      ));
      materialRequests.save(materialRequest(permanentDemoUser, "Ordinateur portable", 1,
          "Besoin pour les expérimentations NLP et les démonstrations de l'équipe."));

      membershipRequests.save(membershipRequest(
          "Omar El Mansouri", "omar.candidat@example.com", MemberType.DOCTORAL, isdiac,
          "Je souhaite rejoindre le LIAS pour préparer une thèse sur l'IA explicable."));

      documents.saveAll(List.of(
          doc(icais, DocumentType.PROGRAM, "Program_ICAIS25.pdf", "/assets/Program_ICAIS25.pdf", admin),
          doc(null, DocumentType.ADMINISTRATIVE, "Logo-LIAS-01.png", "/assets/Logo-LIAS-01.png", admin),
          doc(icisct, DocumentType.ADMINISTRATIVE, "Guide de soumission ICISCT 2026", "https://cmt3.research.microsoft.com/", admin)
      ));

      conventions.saveAll(List.of(
          conv("Université Mohammed V — LRI", "Maroc", "Partenaire académique et réseau de recherche public mentionné par le LIAS."),
          conv("EMSI — LPRI", "Maroc", "Partenaire académique et réseau de recherche public mentionné par le LIAS."),
          conv("Université de Liège", "Belgique", "Partenaire international public mentionné par le LIAS."),
          conv("ABA Technology Maroc", "Maroc", "Partenaire industriel public mentionné par le LIAS."),
          conv("Tech‑IT Maroc", "Maroc", "Partenaire industriel public mentionné par le LIAS."),
          conv("Stefan cel Mare University, Suceava", "Roumanie", "Partenaire international public mentionné par le LIAS.")
      ));

      AuditLog log = new AuditLog();
      log.setActorId(admin.getId());
      log.setAction("SEED_REAL_LIAS_PUBLIC_DATA");
      log.setEntityType("System");
      log.setDetails("Initialisation avec données publiques officielles LIAS : équipes, membres, publications, événements, programme ICAIS’25, ICISCT’26, partenaires et coordonnées.");
      audit.save(log);
    });
  }

  private String requireInitialPassword(String initialPassword) {
    String value = initialPassword == null ? "" : initialPassword.trim();
    if (value.length() < 12) throw new IllegalStateException("INITIAL_ADMIN_PASSWORD must be set to at least 12 characters before seeding LIAS Lab Manager.");
    return value;
  }
  private void saveSetting(SystemSettingRepository settings, String key, String value) { SystemSetting s = new SystemSetting(); s.setSettingKey(key); s.setSettingValue(value); settings.save(s); }
  private User user(String email, String password, AppRole role, PasswordEncoder encoder) { User u = new User(); u.setEmail(email); u.setPasswordHash(encoder.encode(password)); u.setAppRole(role); return u; }
  private Team team(String name, String desc) { Team t = new Team(); t.setName(name); t.setDescription(desc); return t; }
  private Member savePublicMember(UserRepository users, MemberRepository members, PasswordEncoder encoder, String seedPassword, String first, String last, Team team, String interests) { User u = users.save(user(slug(first + "." + last) + "@lias.local", seedPassword, AppRole.PERMANENT_MEMBER, encoder)); return members.save(member(u, first, last, MemberType.PERMANENT, team.getId(), interests, "Membre public du laboratoire LIAS")); }
  private Member member(User u, String first, String last, MemberType type, Long teamId, String interests, String bio) { Member m = new Member(); m.setUserId(u.getId()); m.setFirstName(first); m.setLastName(last); m.setType(type); m.setCurrentTeamId(teamId); m.setAffiliationDate(LocalDate.of(2025, 1, 1)); m.setEstablishment(ESTABLISHMENT); m.setOriginLab(ORIGIN_LAB); m.setBiography(bio); m.setInterests(interests); return m; }
  private RoleHistory role(Member m, RoleName role, LocalDate start, LocalDate end) { RoleHistory r = new RoleHistory(); r.setMemberId(m.getId()); r.setRole(role); r.setStartDate(start); r.setEndDate(end); return r; }
  private Mandate mandate(Member director, Member vice, LocalDate start, LocalDate end) { Mandate m = new Mandate(); m.setDirectorId(director.getId()); m.setViceDirectorId(vice.getId()); m.setStartDate(start); m.setEndDate(end); return m; }
  private TeamChief chief(Mandate mandate, Member m, Team t) { TeamChief c = new TeamChief(); c.setMandateId(mandate.getId()); c.setMemberId(m.getId()); c.setTeamId(t.getId()); c.setStartDate(LocalDate.of(2025, 1, 1)); return c; }
  private Publication publication(String title, String authors, int year, Team team, Member addedBy, String venue) { Publication p = new Publication(); p.setTitle(title); p.setAuthors(authors); p.setYear(year); p.setTeamId(team.getId()); p.setAddedBy(addedBy.getId()); p.setAbstractText("Référence publique LIAS — source/venue : " + venue + "."); p.setPublicationUrl("https://lias.ma/#publications"); return p; }
  private Event event(String title, EventType type, String description, LocalDateTime start, LocalDateTime end, String location, Member organizer, String edition, boolean archived) { Event e = new Event(); e.setTitle(title); e.setType(type); e.setDescription(description); e.setStartDate(start); e.setEndDate(end); e.setLocation(location); e.setOrganizerId(organizer.getId()); e.setEdition(edition); e.setArchived(archived); return e; }
  private Meeting meeting(String title, LocalDateTime date, String agenda, Member createdBy) { Meeting m = new Meeting(); m.setTitle(title); m.setDate(date); m.setAgenda(agenda); m.setCreatedBy(createdBy.getId()); return m; }
  private Message message(User sender, User receiver, Team team, Event event, MessageType type, String content) { Message m = new Message(); m.setSenderId(sender.getId()); if (receiver != null) m.setReceiverId(receiver.getId()); if (team != null) m.setTeamId(team.getId()); if (event != null) m.setEventId(event.getId()); m.setMessageType(type); m.setContent(content); return m; }
  private MaterialInventory material(String name, String description, int quantity, String supplier) { MaterialInventory m = new MaterialInventory(); m.setName(name); m.setDescription(description); m.setQuantity(quantity); m.setSupplier(supplier); m.setReceivedAt(LocalDate.of(2025, 1, 15)); return m; }
  private MaterialRequest materialRequest(User requestedBy, String name, int quantity, String justification) { MaterialRequest r = new MaterialRequest(); r.setRequestedBy(requestedBy.getId()); r.setMaterialName(name); r.setQuantity(quantity); r.setJustification(justification); return r; }
  private MembershipRequest membershipRequest(String applicant, String email, MemberType type, Team team, String motivation) { MembershipRequest r = new MembershipRequest(); r.setApplicantName(applicant); r.setEmail(email); r.setRequestedType(type); r.setPreferredTeamId(team.getId()); r.setEstablishment("Université Hassan II de Casablanca"); r.setOriginLab("Candidat externe"); r.setInterests("Intelligence artificielle explicable"); r.setMotivation(motivation); return r; }
  private DocumentRecord doc(Event event, DocumentType type, String filename, String url, User uploadedBy) { DocumentRecord d = new DocumentRecord(); if (event != null) d.setEventId(event.getId()); d.setType(type); d.setFilename(filename); d.setFileUrl(url); d.setUploadedBy(uploadedBy.getId()); return d; }
  private Convention conv(String partner, String country, String description) { Convention c = new Convention(); c.setPartnerName(partner); c.setPartnerCountry(country); c.setDescription(description); return c; }
  private String slug(String value) { return value.toLowerCase(Locale.ROOT).replace("’", "").replace("'", "").replace("é", "e").replace("è", "e").replace("ê", "e").replace("à", "a").replace("ç", "c").replace("û", "u").replace("î", "i").replaceAll("[^a-z0-9]+", ".").replaceAll("^\\.|\\.$", ""); }
}
