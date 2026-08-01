package ma.lias.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import ma.lias.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
public class JwtService {
  @Value("${app.jwt.secret}") private String secret;
  @Value("${app.jwt.access-minutes}") private long accessMinutes;
  @Value("${app.jwt.refresh-days}") private long refreshDays;
  private SecretKey key(){ return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); }
  public String accessToken(User u){ return token(u, Instant.now().plusSeconds(accessMinutes*60), "access"); }
  public String refreshToken(User u){ return token(u, Instant.now().plusSeconds(refreshDays*86400), "refresh"); }
  private String token(User u, Instant exp, String type){
    return Jwts.builder().subject(u.getEmail()).claim("uid", u.getId()).claim("role", u.getAppRole().name()).claim("typ", type)
      .issuedAt(Date.from(Instant.now())).expiration(Date.from(exp)).signWith(key()).compact();
  }
  public Claims parse(String jwt){ return Jwts.parser().verifyWith(key()).build().parseSignedClaims(jwt).getPayload(); }
}
