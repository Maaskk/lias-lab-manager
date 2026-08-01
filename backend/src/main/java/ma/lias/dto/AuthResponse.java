package ma.lias.dto;
import ma.lias.entity.AppRole;
public record AuthResponse(String accessToken, Long userId, String email, AppRole role) {}
