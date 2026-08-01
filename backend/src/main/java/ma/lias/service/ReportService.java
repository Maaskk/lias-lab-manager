package ma.lias.service;

import ma.lias.entity.*;
import ma.lias.repository.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportService {
  private final AnnualReportRepository reports; private final EventRepository events; private final PublicationRepository publications; private final MemberRepository members; private final MeetingRepository meetings; private final ConventionRepository conventions; private final MaterialInventoryRepository materials;
  @Value("${app.upload-dir:uploads}") private String uploadDir;
  public ReportService(AnnualReportRepository reports, EventRepository events, PublicationRepository publications, MemberRepository members, MeetingRepository meetings, ConventionRepository conventions, MaterialInventoryRepository materials){this.reports=reports;this.events=events;this.publications=publications;this.members=members;this.meetings=meetings;this.conventions=conventions;this.materials=materials;}
  public AnnualReport generate(int year, Long generatedBy) throws Exception {
    Path dir=Path.of(uploadDir,"reports"); Files.createDirectories(dir); String filename="rapport_annuel_"+year+".pdf"; Path out=dir.resolve(filename);
    List<Event> eventsForYear=events.findAll().stream().filter(e -> e.getStartDate()!=null && e.getStartDate().getYear()==year).toList();
    List<Publication> publicationsForYear=publications.findAll().stream().filter(p -> p.getYear()==year).toList();
    List<Meeting> meetingsForYear=meetings.findAll().stream().filter(m -> m.getDate()!=null && m.getDate().getYear()==year).toList();
    List<Convention> conventionsForYear=conventions.findAll().stream().filter(c -> (c.getStartDate()!=null && c.getStartDate().getYear()<=year) && (c.getEndDate()==null || c.getEndDate().getYear()>=year)).toList();
    List<MaterialInventory> materialsForYear=materials.findAll().stream().filter(m -> m.getReceivedAt()==null || m.getReceivedAt().getYear()==year).toList();
    try(PDDocument doc=new PDDocument()){
      PDPage page=new PDPage(PDRectangle.A4); doc.addPage(page);
      try(PDPageContentStream cs=new PDPageContentStream(doc,page)){
        PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        cs.beginText(); cs.setFont(bold,18); cs.newLineAtOffset(50,790); cs.showText("LIAS - Rapport d'activité annuel " + year); cs.endText();
        int y=750;
        List<String> lines=List.of(
          "Membres enregistrés : "+members.count(),
          "Événements de l'année : "+eventsForYear.size(),
          "Publications de l'année : "+publicationsForYear.size(),
          "Réunions/PV de l'année : "+meetingsForYear.size(),
          "Conventions actives : "+conventionsForYear.size(),
          "Matériel reçu ou disponible : "+materialsForYear.size(),
          "Généré le : "+ LocalDateTime.now()
        );
        for(String line:lines){ cs.beginText(); cs.setFont(normal,12); cs.newLineAtOffset(60,y); cs.showText(line); cs.endText(); y-=24; }
        cs.beginText(); cs.setFont(bold,14); cs.newLineAtOffset(50,y-15); cs.showText("Synthèse"); cs.endText(); y-=45;
        String summary="Ce rapport a été généré automatiquement depuis les modules événements, publications, membres, matériel, conventions et réunions du système.";
        cs.beginText(); cs.setFont(normal,11); cs.newLineAtOffset(60,y); cs.showText(summary); cs.endText();
      }
      doc.save(out.toFile());
    }
    AnnualReport r=new AnnualReport(); r.setYear(year); r.setFileUrl("/uploads/reports/"+filename); r.setGeneratedBy(generatedBy); return reports.save(r);
  }
}
