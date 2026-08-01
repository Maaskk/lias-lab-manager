package ma.lias.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class JwtSecretValidator {
  @Value("${app.jwt.secret}") private String secret;

  @PostConstruct
  public void validate(){
    if(secret == null || secret.length() < 64){
      throw new IllegalStateException("JWT_SECRET must be set to at least 64 characters before starting LIAS Lab Manager.");
    }
  }
}
