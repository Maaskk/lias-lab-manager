package ma.lias.controller;

import ma.lias.entity.*;
import ma.lias.repository.*;
import ma.lias.service.AuditService;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/material-distributions")
public class MaterialDistributionController {
  private final MaterialDistributionRepository distributions;
  private final MaterialInventoryRepository inventory;
  private final MemberRepository members;
  private final AuditService audit;

  public MaterialDistributionController(MaterialDistributionRepository distributions, MaterialInventoryRepository inventory, MemberRepository members, AuditService audit) {
    this.distributions = distributions;
    this.inventory = inventory;
    this.members = members;
    this.audit = audit;
  }

  @GetMapping
  public java.util.List<MaterialDistribution> all() { return distributions.findAll(); }

  @PostMapping
  public ResponseEntity<MaterialDistribution> create(@RequestBody MaterialDistribution body, Authentication auth) {
    MaterialInventory item = inventory.findById(body.getMaterialId())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Matériel introuvable"));
    if (body.getMemberId() == null || !members.existsById(body.getMemberId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Membre bénéficiaire invalide");
    }
    if (body.getQuantity() <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantité invalide");
    if (item.getQuantity() < body.getQuantity()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuffisant");
    item.setQuantity(item.getQuantity()-body.getQuantity());
    inventory.save(item);
    if (auth != null && auth.getPrincipal() instanceof User user) {
      members.findByUserId(user.getId()).ifPresent(member -> body.setDistributedBy(member.getId()));
    }
    MaterialDistribution saved = distributions.save(body);
    audit.log("CREATE", "MaterialDistribution", saved.getId(), "Distribution matériel avec décrément du stock");
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }
}
