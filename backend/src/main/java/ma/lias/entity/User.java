package ma.lias.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable=false, unique=true) private String email;
  @JsonIgnore @Column(name="password_hash", nullable=false) private String passwordHash;
  @Enumerated(EnumType.STRING) @Column(nullable=false) private UserStatus status = UserStatus.ACTIVE;
  @Enumerated(EnumType.STRING) @Column(name="app_role", nullable=false) private AppRole appRole = AppRole.PERMANENT_MEMBER;
  @Column(name="created_at", nullable=false) private LocalDateTime createdAt = LocalDateTime.now();
  public Long getId(){return id;} public void setId(Long id){this.id=id;}
  public String getEmail(){return email;} public void setEmail(String email){this.email=email;}
  public String getPasswordHash(){return passwordHash;} public void setPasswordHash(String passwordHash){this.passwordHash=passwordHash;}
  public UserStatus getStatus(){return status;} public void setStatus(UserStatus status){this.status=status;}
  public AppRole getAppRole(){return appRole;} public void setAppRole(AppRole appRole){this.appRole=appRole;}
  public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime createdAt){this.createdAt=createdAt;}
}
