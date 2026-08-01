package ma.lias.controller;

import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import ma.lias.dto.*;
import ma.lias.entity.*;
import ma.lias.repository.UserRepository;
import ma.lias.security.JwtService;
import ma.lias.service.AuditService;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Duration;
import java.util.Map;

@RestController @RequestMapping("/api/auth")
public class AuthController {
  private final UserRepository users; private final PasswordEncoder encoder; private final JwtService jwt; private final AuditService audit;
  public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt, AuditService audit){this.users=users;this.encoder=encoder;this.jwt=jwt;this.audit=audit;}
  @PostMapping("/login") public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req, HttpServletResponse response){
    User u = users.findByEmail(req.email()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Identifiants invalides"));
    if(u.getStatus()!=UserStatus.ACTIVE || !encoder.matches(req.password(), u.getPasswordHash())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Identifiants invalides");
    String refresh=jwt.refreshToken(u); ResponseCookie cookie=ResponseCookie.from("refresh_token", refresh).httpOnly(true).secure(false).sameSite("Lax").path("/api/auth").maxAge(Duration.ofDays(7)).build(); response.addHeader(HttpHeaders.SET_COOKIE,cookie.toString()); audit.log("LOGIN","User",u.getId(),"Connexion réussie");
    return ResponseEntity.ok(new AuthResponse(jwt.accessToken(u),u.getId(),u.getEmail(),u.getAppRole()));
  }
  @PostMapping("/refresh") public AuthResponse refresh(@CookieValue(name="refresh_token", required=false) String token){
    if(token==null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Session expirée");
    var claims=jwt.parse(token); if(!"refresh".equals(claims.get("typ",String.class))) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Token invalide");
    User u=users.findByEmail(claims.getSubject()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Utilisateur introuvable"));
    return new AuthResponse(jwt.accessToken(u),u.getId(),u.getEmail(),u.getAppRole());
  }
  @PostMapping("/logout") public ResponseEntity<Map<String,String>> logout(HttpServletResponse response){ ResponseCookie cookie=ResponseCookie.from("refresh_token","").httpOnly(true).sameSite("Lax").path("/api/auth").maxAge(0).build(); response.addHeader(HttpHeaders.SET_COOKIE,cookie.toString()); return ResponseEntity.ok(Map.of("message","Déconnexion réussie")); }
  @GetMapping("/me") public Object me(org.springframework.security.core.Authentication auth){ return auth==null?Map.of("authenticated",false):auth.getPrincipal(); }
}
