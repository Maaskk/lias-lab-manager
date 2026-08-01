package ma.lias.security;

import io.jsonwebtoken.Claims;
import ma.lias.entity.User;
import ma.lias.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwtService; private final UserRepository users;
  public JwtAuthenticationFilter(JwtService jwtService, UserRepository users){ this.jwtService=jwtService; this.users=users; }
  @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
    String h = request.getHeader("Authorization");
    if(h != null && h.startsWith("Bearer ")){
      try{
        Claims claims = jwtService.parse(h.substring(7));
        users.findByEmail(claims.getSubject()).ifPresent(u -> {
          if(u.getStatus().name().equals("ACTIVE")){
            var auth = new UsernamePasswordAuthenticationToken(u, null, List.of(new SimpleGrantedAuthority("ROLE_"+u.getAppRole().name())));
            SecurityContextHolder.getContext().setAuthentication(auth);
          }
        });
      } catch(Exception ignored){ SecurityContextHolder.clearContext(); }
    }
    chain.doFilter(request, response);
  }
}
