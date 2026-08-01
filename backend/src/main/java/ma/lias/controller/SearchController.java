package ma.lias.controller;

import ma.lias.entity.*;
import ma.lias.repository.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/search")
public class SearchController {
  private final MemberRepository members; private final DocumentRecordRepository documents; private final EventRepository events; private final PublicationRepository publications;
  public SearchController(MemberRepository members, DocumentRecordRepository documents, EventRepository events, PublicationRepository publications){this.members=members;this.documents=documents;this.events=events;this.publications=publications;}

  @GetMapping public Map<String,Object> search(@RequestParam(defaultValue="") String q){
    String needle=q.toLowerCase(Locale.ROOT).trim();
    if(needle.length()<2) return Map.of("members",List.of(),"documents",List.of(),"events",List.of(),"publications",List.of());
    var memberResults = members.findAll().stream().filter(m -> contains(needle, m.getFirstName(), m.getLastName(), m.getInterests(), m.getBiography(), m.getEstablishment(), m.getOriginLab())).limit(20).map(this::safeMember).toList();
    return Map.of(
      "members", memberResults,
      "documents", documents.findAll().stream().filter(d -> contains(needle, d.getFilename(), d.getType()==null?null:d.getType().name())).limit(20).toList(),
      "events", events.findAll().stream().filter(e -> contains(needle, e.getTitle(), e.getDescription(), e.getLocation(), e.getEdition(), e.getType()==null?null:e.getType().name())).limit(20).toList(),
      "publications", publications.findAll().stream().filter(p -> contains(needle, p.getTitle(), p.getAuthors(), p.getAbstractText(), p.getDoi(), p.getPublicationUrl(), String.valueOf(p.getYear()))).limit(20).toList()
    );
  }

  private boolean contains(String needle, String... values){
    for(String value: values) if(value!=null && value.toLowerCase(Locale.ROOT).contains(needle)) return true;
    return false;
  }
  private Map<String,Object> safeMember(Member member){
    Map<String,Object> out=new LinkedHashMap<>();
    out.put("id",member.getId());
    out.put("firstName",member.getFirstName());
    out.put("lastName",member.getLastName());
    out.put("photoUrl",member.getPhotoUrl());
    out.put("type",member.getType());
    out.put("currentTeamId",member.getCurrentTeamId());
    out.put("interests",member.getInterests());
    out.put("establishment",member.getEstablishment());
    return out;
  }
}
