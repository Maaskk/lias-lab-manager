package ma.lias.controller;

import ma.lias.repository.*;
import ma.lias.entity.Member;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/public")
public class PublicController {
  private final TeamRepository teams; private final MemberRepository members; private final EventRepository events; private final PublicationRepository publications;
  public PublicController(TeamRepository teams, MemberRepository members, EventRepository events, PublicationRepository publications){this.teams=teams;this.members=members;this.events=events;this.publications=publications;}
  @GetMapping("/stats") public Map<String,Object> stats(){ return Map.of("members",members.count(),"teams",teams.count(),"events",events.count(),"publications",publications.count()); }
  @GetMapping("/teams") public Object publicTeams(){ return teams.findAll(); }
  @GetMapping("/teams/{id}") public Map<String,Object> team(@PathVariable Long id){ return Map.of("team",teams.findById(id).orElseThrow(),"members",members.findByCurrentTeamId(id).stream().map(this::publicMember).toList(),"publications",publications.findAll().stream().filter(p -> Objects.equals(p.getTeamId(), id)).toList()); }
  @GetMapping("/events") public Object publicEvents(){ return events.findAll(); }
  @GetMapping("/events/{id}") public Object event(@PathVariable Long id){ return events.findById(id).orElseThrow(); }
  @GetMapping("/publications") public Object publicPublications(){ return publications.findAll(); }
  @GetMapping("/publications/{id}") public Object publication(@PathVariable Long id){ return publications.findById(id).orElseThrow(); }
  private Map<String,Object> publicMember(Member member){ Map<String,Object> out=new LinkedHashMap<>(); out.put("id",member.getId()); out.put("firstName",member.getFirstName()); out.put("lastName",member.getLastName()); out.put("photoUrl",member.getPhotoUrl()); out.put("biography",member.getBiography()); out.put("interests",member.getInterests()); out.put("establishment",member.getEstablishment()); out.put("originLab",member.getOriginLab()); out.put("currentTeamId",member.getCurrentTeamId()); out.put("type",member.getType()); return out; }
}
