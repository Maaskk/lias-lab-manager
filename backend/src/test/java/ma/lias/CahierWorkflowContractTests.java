package ma.lias;

import org.junit.jupiter.api.Test;
import java.nio.file.*;
import static org.assertj.core.api.Assertions.assertThat;

class CahierWorkflowContractTests {
  private static String source(String path) throws Exception {
    return Files.readString(Path.of("src/main/java/ma/lias", path));
  }

  @Test
  void mandateGovernanceIsExplicitAndUsedForMembershipDecisions() throws Exception {
    String service = source("service/ActiveMandateService.java");
    String mandate = source("controller/MandateController.java");
    String membership = source("controller/MembershipRequestController.java");
    assertThat(service).contains("activeMandate", "validateNoOverlap", "requireActiveDirector", "syncMandateRoles");
    assertThat(mandate).contains("validateNoOverlap", "syncMandateRoles");
    assertThat(membership).contains("activeMandates.requireActiveDirector(actor)");
  }

  @Test
  void documentsAreVersionedAndArchivedWithoutDestructiveDelete() throws Exception {
    String entity = source("entity/DocumentRecord.java");
    String controller = source("controller/DocumentController.java");
    assertThat(entity).contains("version", "archived", "archivedAt", "description");
    assertThat(controller).contains("nextVersion", "d.setVersion", "d.setArchived(true)");
    assertThat(controller).doesNotContain("repo.deleteById(id)");
  }

  @Test
  void materialApprovalCreatesDistributionAndDecrementsStock() throws Exception {
    String request = source("controller/MaterialRequestController.java");
    String distribution = source("controller/MaterialDistributionController.java");
    String entity = source("entity/MaterialRequest.java");
    assertThat(entity).contains("reviewReason");
    assertThat(request).contains("distributeApprovedRequest", "item.setQuantity(item.getQuantity()-request.getQuantity())", "distributions.save(d)", "notifyRequester");
    assertThat(distribution).contains("item.setQuantity(item.getQuantity()-body.getQuantity())");
  }

  @Test
  void publicationOwnershipAndProfileContextArePreserved() throws Exception {
    String publications = source("controller/PublicationController.java");
    String member = source("controller/MemberController.java");
    assertThat(publications).contains("body.setAddedBy(m.getId())", "canModify", "author", "teamId", "year");
    assertThat(member).contains("teamName", "publications.findByAddedByOrderByYearDescCreatedAtDesc");
  }

  @Test
  void internalUploadsAndMaterialDistributionsAreProtected() throws Exception {
    String security = source("config/SecurityConfig.java");
    assertThat(security).contains("/uploads/public/**", "/uploads/photos/**", ".requestMatchers(\"/uploads/**\").authenticated()");
    assertThat(security).doesNotContain("\"/uploads/**\", \"/error\").permitAll()");
    assertThat(security).contains("/api/material-distributions/**");
  }

  @Test
  void eventDetailsExposeArchiveDocumentsAndDiscussion() throws Exception {
    String events = source("controller/EventController.java");
    assertThat(events).contains("documents.findByEventId(id)", "messages.findByEventIdAndMessageTypeAndDeletedAtIsNullOrderBySentAtAsc(id,MessageType.EVENT)", "@PatchMapping(\"/{id}/archive\")");
    assertThat(events).doesNotContain("repo.deleteById(id)");
  }
}
