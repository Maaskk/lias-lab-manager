package ma.lias.dto;
import jakarta.validation.constraints.*;
public record LoginRequest(@Email @NotBlank String email, @NotBlank @Size(max=128) String password) {}
