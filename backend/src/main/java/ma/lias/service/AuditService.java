package ma.lias.service;

import ma.lias.entity.*;
import ma.lias.repository.AuditLogRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
  private final AuditLogRepository audit;
  public AuditService(AuditLogRepository audit){this.audit=audit;}
  public void log(String action, String entityType, Long entityId, String details){
    AuditLog a = new AuditLog();
    Object principal = SecurityContextHolder.getContext().getAuthentication()==null?null:SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if(principal instanceof User u) a.setActorId(u.getId());
    a.setAction(action); a.setEntityType(entityType); a.setEntityId(entityId); a.setDetails(details);
    audit.save(a);
  }
}
