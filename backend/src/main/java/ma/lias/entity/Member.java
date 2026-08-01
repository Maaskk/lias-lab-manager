package ma.lias.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity @Table(name="members")
public class Member {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="user_id") private Long userId;
  @Column(name="first_name", nullable=false) private String firstName;
  @Column(name="last_name", nullable=false) private String lastName;
  @Column(name="photo_url") private String photoUrl;
  @Column(name="birth_date") private LocalDate birthDate;
  @Column(name="hire_date") private LocalDate hireDate;
  @Column(name="affiliation_date") private LocalDate affiliationDate;
  @Column(length=4000) private String biography;
  @Column(length=2000) private String interests;
  private String establishment;
  @Column(name="origin_lab") private String originLab;
  @Column(name="current_team_id") private Long currentTeamId;
  @Enumerated(EnumType.STRING) @Column(nullable=false) private MemberType type = MemberType.PERMANENT;
  public Long getId(){return id;} public void setId(Long id){this.id=id;}
  public Long getUserId(){return userId;} public void setUserId(Long userId){this.userId=userId;}
  public String getFirstName(){return firstName;} public void setFirstName(String firstName){this.firstName=firstName;}
  public String getLastName(){return lastName;} public void setLastName(String lastName){this.lastName=lastName;}
  public String getPhotoUrl(){return photoUrl;} public void setPhotoUrl(String photoUrl){this.photoUrl=photoUrl;}
  public LocalDate getBirthDate(){return birthDate;} public void setBirthDate(LocalDate birthDate){this.birthDate=birthDate;}
  public LocalDate getHireDate(){return hireDate;} public void setHireDate(LocalDate hireDate){this.hireDate=hireDate;}
  public LocalDate getAffiliationDate(){return affiliationDate;} public void setAffiliationDate(LocalDate affiliationDate){this.affiliationDate=affiliationDate;}
  public String getBiography(){return biography;} public void setBiography(String biography){this.biography=biography;}
  public String getInterests(){return interests;} public void setInterests(String interests){this.interests=interests;}
  public String getEstablishment(){return establishment;} public void setEstablishment(String establishment){this.establishment=establishment;}
  public String getOriginLab(){return originLab;} public void setOriginLab(String originLab){this.originLab=originLab;}
  public Long getCurrentTeamId(){return currentTeamId;} public void setCurrentTeamId(Long currentTeamId){this.currentTeamId=currentTeamId;}
  public MemberType getType(){return type;} public void setType(MemberType type){this.type=type;}
}
