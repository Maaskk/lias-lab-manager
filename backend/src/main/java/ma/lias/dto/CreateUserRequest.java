package ma.lias.dto;
import jakarta.validation.constraints.*;
import ma.lias.entity.*;
public record CreateUserRequest(@Email @NotBlank String email, @NotBlank @Size(min=12,max=128) String password, AppRole appRole, UserStatus status) {}
