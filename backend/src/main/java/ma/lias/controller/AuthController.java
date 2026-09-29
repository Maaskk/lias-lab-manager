package ma.lias.controller;

import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import ma.lias.dto.*;
import ma.lias.entity.*;
import ma.lias.repository.UserRepository;
import ma.lias.security.JwtService;
import ma.lias.service.AuditService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final JwtService jwt;
  private final AuditService audit;

  @Value("${app.cookies.secure:false}")
  private boolean secureCookie;

  @Value("${app.cookies.same-site:Lax}")
  private String sameSite;

  public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt, AuditService audit) {
    this.users = users;
    this.encoder = encoder;
    this.jwt = jwt;
    this.audit = audit;
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
    String email = request.email().trim().toLowerCase(Locale.ROOT);
    User user = users.findByEmail(email)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides"));
    if (user.getStatus() != UserStatus.ACTIVE || !encoder.matches(request.password(), user.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
    }
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie(jwt.refreshToken(user), Duration.ofDays(7)).toString());
    audit.log("LOGIN", "User", user.getId(), "Connexion réussie");
    return ResponseEntity.ok(new AuthResponse(jwt.accessToken(user), user.getId(), user.getEmail(), user.getAppRole()));
  }

  @PostMapping("/refresh")
  public AuthResponse refresh(@CookieValue(name = "refresh_token", required = false) String token) {
    if (token == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expirée");
    io.jsonwebtoken.Claims claims;
    try {
      claims = jwt.parse(token);
    } catch (Exception invalidToken) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expirée");
    }
    if (!"refresh".equals(claims.get("typ", String.class))) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalide");
    }
    User user = users.findByEmail(claims.getSubject())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session invalide"));
    if (user.getStatus() != UserStatus.ACTIVE) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Compte inactif");
    }
    return new AuthResponse(jwt.accessToken(user), user.getId(), user.getEmail(), user.getAppRole());
  }

  @PostMapping("/logout")
  public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie("", Duration.ZERO).toString());
    return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
  }

  @GetMapping("/me")
  public Object me(org.springframework.security.core.Authentication auth) {
    return auth == null ? Map.of("authenticated", false) : auth.getPrincipal();
  }

  private ResponseCookie refreshCookie(String value, Duration maxAge) {
    return ResponseCookie.from("refresh_token", value)
      .httpOnly(true)
      .secure(secureCookie)
      .sameSite(sameSite)
      .path("/api/auth")
      .maxAge(maxAge)
      .build();
  }
}
