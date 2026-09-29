package ma.lias.config;

import ma.lias.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.*;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
  @Value("${app.cors-origins}") private String origins;
  @Bean PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(); }
  @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception { return configuration.getAuthenticationManager(); }
  @Bean CorsConfigurationSource corsConfigurationSource(){
    CorsConfiguration c = new CorsConfiguration();
    c.setAllowedOrigins(Arrays.asList(origins.split(",")));
    c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
    c.setAllowedHeaders(List.of("Authorization","Content-Type","Accept"));
    c.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", c);
    return source;
  }
  @Bean SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
    http.csrf(csrf -> csrf.disable()).cors(cors -> cors.configurationSource(corsConfigurationSource()))
      .headers(headers -> headers
        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'self'; base-uri 'self'; form-action 'self'; object-src 'none'"))
        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
        .frameOptions(frame -> frame.sameOrigin()))
      .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/auth/**", "/api/public/**", "/uploads/public/**", "/uploads/photos/**", "/error").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/membership-requests/public").permitAll()
        .requestMatchers("/uploads/**").authenticated()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.GET, "/api/publications/**").hasAnyRole("ADMIN","DIRECTOR","VICE_DIRECTOR","TEAM_CHIEF","PERMANENT_MEMBER","ASSOCIATE_MEMBER","DOCTORAL")
        .requestMatchers(HttpMethod.POST, "/api/publications").hasAnyRole("ADMIN","DIRECTOR","VICE_DIRECTOR","TEAM_CHIEF","PERMANENT_MEMBER","ASSOCIATE_MEMBER","DOCTORAL")
        .requestMatchers(HttpMethod.PUT, "/api/publications/**").hasAnyRole("ADMIN","DIRECTOR","VICE_DIRECTOR","TEAM_CHIEF","PERMANENT_MEMBER","ASSOCIATE_MEMBER","DOCTORAL")
        .requestMatchers(HttpMethod.DELETE, "/api/publications/**").hasAnyRole("ADMIN","DIRECTOR","VICE_DIRECTOR")
        .requestMatchers(HttpMethod.GET, "/api/members/me").hasAnyRole("ADMIN","DIRECTOR","VICE_DIRECTOR","TEAM_CHIEF","PERMANENT_MEMBER","ASSOCIATE_MEMBER","DOCTORAL")
        .requestMatchers(HttpMethod.GET, "/api/dashboard", "/api/members", "/api/members/*", "/api/teams/**", "/api/events/**", "/api/calendar/**").hasAnyRole("ADMIN","DIRECTOR","VICE_DIRECTOR","TEAM_CHIEF","PERMANENT_MEMBER","ASSOCIATE_MEMBER")
        .requestMatchers(HttpMethod.PUT, "/api/members/**").hasAnyRole("ADMIN","DIRECTOR","VICE_DIRECTOR","TEAM_CHIEF","PERMANENT_MEMBER","ASSOCIATE_MEMBER","DOCTORAL")
        .requestMatchers(HttpMethod.POST, "/api/members/*/photo").hasAnyRole("ADMIN","DIRECTOR","VICE_DIRECTOR","TEAM_CHIEF","PERMANENT_MEMBER","ASSOCIATE_MEMBER","DOCTORAL")
        .requestMatchers(HttpMethod.PATCH, "/api/members/**").hasAnyRole("ADMIN","DIRECTOR","VICE_DIRECTOR")
        .requestMatchers(HttpMethod.POST, "/api/events/**", "/api/documents/**", "/api/material/**", "/api/meetings/**", "/api/conventions/**").hasAnyRole("ADMIN","DIRECTOR","VICE_DIRECTOR","TEAM_CHIEF")
        .requestMatchers(HttpMethod.PUT, "/api/events/**", "/api/documents/**", "/api/material/**", "/api/meetings/**", "/api/conventions/**").hasAnyRole("ADMIN","DIRECTOR","VICE_DIRECTOR","TEAM_CHIEF")
        .requestMatchers(HttpMethod.DELETE, "/api/events/**", "/api/documents/**", "/api/material/**", "/api/meetings/**", "/api/conventions/**").hasAnyRole("ADMIN","DIRECTOR","VICE_DIRECTOR")
        .requestMatchers("/api/reports/**", "/api/annual-reports/**", "/api/membership-requests/**").hasAnyRole("DIRECTOR","ADMIN")
        .requestMatchers(HttpMethod.POST, "/api/material-requests/*/decision").hasAnyRole("DIRECTOR","VICE_DIRECTOR","ADMIN")
        .requestMatchers(HttpMethod.POST, "/api/material-distributions/**").hasAnyRole("DIRECTOR","VICE_DIRECTOR","ADMIN")
        .requestMatchers(HttpMethod.GET, "/api/material-distributions/**").hasAnyRole("DIRECTOR","VICE_DIRECTOR","ADMIN","TEAM_CHIEF","PERMANENT_MEMBER")
        .requestMatchers(HttpMethod.GET, "/api/search").hasAnyRole("ADMIN","DIRECTOR","VICE_DIRECTOR","TEAM_CHIEF","PERMANENT_MEMBER","ASSOCIATE_MEMBER")
        .requestMatchers("/api/messages/**").hasAnyRole("ADMIN","DIRECTOR","VICE_DIRECTOR","TEAM_CHIEF","PERMANENT_MEMBER","ASSOCIATE_MEMBER")
        .requestMatchers("/api/notifications/**").hasAnyRole("ADMIN","DIRECTOR","VICE_DIRECTOR","TEAM_CHIEF","PERMANENT_MEMBER","ASSOCIATE_MEMBER","DOCTORAL")
        .requestMatchers("/api/**").hasAnyRole("ADMIN","DIRECTOR","VICE_DIRECTOR","TEAM_CHIEF","PERMANENT_MEMBER")
        .anyRequest().authenticated())
      .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
