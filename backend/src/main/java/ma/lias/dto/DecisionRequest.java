package ma.lias.dto;
import ma.lias.entity.AppRole;
import ma.lias.entity.MemberType;

public record DecisionRequest(boolean accepted, String reason, MemberType memberType, AppRole appRole) {}
