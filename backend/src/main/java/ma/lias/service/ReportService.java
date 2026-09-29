package ma.lias.service;

import ma.lias.entity.*;
import ma.lias.repository.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReportService {
  private final AnnualReportRepository reports;
  private final EventRepository events;
  private final PublicationRepository publications;
  private final MemberRepository members;
  private final MeetingRepository meetings;
  private final ConventionRepository conventions;
  private final MaterialInventoryRepository materials;

  @Value("${app.upload-dir:uploads}")
  private String uploadDir;

  public ReportService(
      AnnualReportRepository reports,
      EventRepository events,
      PublicationRepository publications,
      MemberRepository members,
      MeetingRepository meetings,
      ConventionRepository conventions,
      MaterialInventoryRepository materials) {
    this.reports = reports;
    this.events = events;
    this.publications = publications;
    this.members = members;
    this.meetings = meetings;
    this.conventions = conventions;
    this.materials = materials;
  }

  public AnnualReport generate(int year, Long generatedBy) throws Exception {
    if (year < 2000 || year > 2100) throw new IllegalArgumentException("Année de rapport invalide.");

    Path directory = Path.of(uploadDir, "reports");
    Files.createDirectories(directory);
    String filename = "rapport_annuel_" + year + ".pdf";
    Path output = directory.resolve(filename);

    List<Event> eventsForYear = events.findAll().stream()
      .filter(event -> event.getStartDate() != null && event.getStartDate().getYear() == year)
      .sorted(Comparator.comparing(Event::getStartDate))
      .toList();
    List<Publication> publicationsForYear = publications.findAll().stream()
      .filter(publication -> Objects.equals(publication.getYear(), year))
      .sorted(Comparator.comparing(Publication::getTitle, Comparator.nullsLast(String::compareToIgnoreCase)))
      .toList();
    List<Meeting> meetingsForYear = meetings.findAll().stream()
      .filter(meeting -> meeting.getDate() != null && meeting.getDate().getYear() == year)
      .sorted(Comparator.comparing(Meeting::getDate))
      .toList();
    List<Convention> conventionsForYear = conventions.findAll().stream()
      .filter(convention -> convention.getStartDate() == null || convention.getStartDate().getYear() <= year)
      .filter(convention -> convention.getEndDate() == null || convention.getEndDate().getYear() >= year)
      .toList();
    List<MaterialInventory> materialsForYear = materials.findAll().stream()
      .filter(material -> material.getReceivedAt() == null || material.getReceivedAt().getYear() == year)
      .toList();
    List<Member> allMembers = members.findAll();

    try (PDDocument document = new PDDocument()) {
      PdfReportWriter writer = new PdfReportWriter(document);
      writer.title("LIAS - Rapport d'activité annuel " + year);
      writer.paragraph("Rapport généré automatiquement depuis la mémoire institutionnelle du laboratoire le "
        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")) + ".");

      writer.section("Synthèse générale");
      writer.bullet("Membres enregistrés : " + allMembers.size());
      writer.bullet("Événements de l'année : " + eventsForYear.size());
      writer.bullet("Publications de l'année : " + publicationsForYear.size());
      writer.bullet("Réunions/PV de l'année : " + meetingsForYear.size());
      writer.bullet("Conventions et partenariats actifs : " + conventionsForYear.size());
      writer.bullet("Références de matériel reçues ou disponibles : " + materialsForYear.size());

      writer.section("Répartition des membres");
      for (MemberType type : MemberType.values()) {
        long count = allMembers.stream().filter(member -> member.getType() == type).count();
        writer.bullet(label(type) + " : " + count);
      }

      writer.section("Activités scientifiques");
      if (eventsForYear.isEmpty()) writer.paragraph("Aucun événement enregistré pour cette année.");
      for (Event event : eventsForYear) {
        writer.bullet(event.getTitle() + " - " + event.getStartDate().toLocalDate()
          + (event.getEdition() == null ? "" : " - " + event.getEdition()));
      }

      writer.section("Publications");
      if (publicationsForYear.isEmpty()) writer.paragraph("Aucune publication enregistrée pour cette année.");
      for (Publication publication : publicationsForYear) {
        writer.bullet(publication.getTitle() + " - " + value(publication.getAuthors()));
      }

      writer.section("Réunions et procès-verbaux");
      if (meetingsForYear.isEmpty()) writer.paragraph("Aucune réunion enregistrée pour cette année.");
      for (Meeting meeting : meetingsForYear) {
        writer.bullet(meeting.getTitle() + " - " + meeting.getDate().toLocalDate()
          + (meeting.getPvUrl() == null ? " - PV en attente" : " - PV archivé"));
      }

      writer.section("Conventions et partenariats");
      if (conventionsForYear.isEmpty()) writer.paragraph("Aucune convention active pour cette année.");
      for (Convention convention : conventionsForYear) {
        writer.bullet(convention.getPartnerName() + " - " + value(convention.getPartnerCountry()));
      }

      writer.section("Matériel et équipements");
      if (materialsForYear.isEmpty()) writer.paragraph("Aucun matériel reçu ou disponible pour cette année.");
      for (MaterialInventory material : materialsForYear) {
        writer.bullet(material.getName() + " - quantité disponible : " + material.getQuantity());
      }

      writer.finish();
      document.save(output.toFile());
    }

    AnnualReport report = new AnnualReport();
    report.setYear(year);
    report.setFileUrl("/uploads/reports/" + filename);
    report.setGeneratedBy(generatedBy);
    return reports.save(report);
  }

  private String label(MemberType type) {
    return switch (type) {
      case PERMANENT -> "Membres permanents";
      case ASSOCIATE -> "Membres associés";
      case DOCTORAL -> "Doctorants";
      case RETIRED -> "Membres retraités";
      case FORMER -> "Anciens membres";
    };
  }

  private String value(String text) { return text == null || text.isBlank() ? "Non renseigné" : text; }

  private static final class PdfReportWriter {
    private static final float LEFT = 50;
    private static final float TOP = 795;
    private static final float BOTTOM = 55;
    private final PDDocument document;
    private final PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private final PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private PDPageContentStream stream;
    private float y;

    private PdfReportWriter(PDDocument document) throws Exception {
      this.document = document;
      newPage();
    }

    private void title(String text) throws Exception {
      writeWrapped(text, bold, 18, 24, 72, "");
      y -= 8;
    }

    private void section(String text) throws Exception {
      ensureSpace(36);
      y -= 10;
      writeWrapped(text, bold, 14, 19, 82, "");
    }

    private void paragraph(String text) throws Exception {
      writeWrapped(text, regular, 10, 15, 94, "");
      y -= 4;
    }

    private void bullet(String text) throws Exception {
      writeWrapped(text, regular, 10, 15, 90, "- ");
    }

    private void writeWrapped(String text, PDFont font, float size, float lineHeight, int maxChars, String prefix) throws Exception {
      List<String> lines = wrap(prefix + safe(text), maxChars);
      for (String line : lines) {
        ensureSpace(lineHeight);
        stream.beginText();
        stream.setFont(font, size);
        stream.newLineAtOffset(LEFT, y);
        stream.showText(line);
        stream.endText();
        y -= lineHeight;
      }
    }

    private void ensureSpace(float needed) throws Exception {
      if (y - needed < BOTTOM) newPage();
    }

    private void newPage() throws Exception {
      if (stream != null) stream.close();
      PDPage page = new PDPage(PDRectangle.A4);
      document.addPage(page);
      stream = new PDPageContentStream(document, page);
      y = TOP;
    }

    private void finish() throws Exception {
      if (stream != null) {
        stream.close();
        stream = null;
      }
    }

    private static List<String> wrap(String text, int maxChars) {
      List<String> lines = new ArrayList<>();
      StringBuilder current = new StringBuilder();
      for (String word : text.split("\\s+")) {
        if (!current.isEmpty() && current.length() + word.length() + 1 > maxChars) {
          lines.add(current.toString());
          current.setLength(0);
        }
        if (!current.isEmpty()) current.append(' ');
        current.append(word);
      }
      if (!current.isEmpty()) lines.add(current.toString());
      if (lines.isEmpty()) lines.add("");
      return lines;
    }

    private static String safe(String text) {
      return text == null ? "" : text
        .replace('’', '\'')
        .replace('‘', '\'')
        .replace('“', '"')
        .replace('”', '"')
        .replace('‑', '-')
        .replace('–', '-')
        .replace('—', '-')
        .replace('•', '-')
        .replace('…', '.')
        .replace('\u00a0', ' ')
        .replace('\u202f', ' ');
    }
  }
}
