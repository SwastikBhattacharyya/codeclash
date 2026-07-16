package com.codeclash.authservice.user.adapter.inbound.rest.dto;

import com.codeclash.authservice.user.model.Id;
import com.codeclash.authservice.user.model.Name;
import com.codeclash.authservice.user.model.User;
import com.codeclash.authservice.user.model.Username;
import com.codeclash.contracts.auth.AuthPermissions;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "A user; email and roleId are hidden unless the caller may read them")
public record UserResponseBody(
    @Schema(
            description = "User id (UUID version " + Id.VERSION + ")",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f71-0f1a-7b2c-9d3e-4f5a6b7c8d9e")
        UUID id,
    @Schema(
            description = "Display name",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            minLength = Name.MIN_SIZE,
            maxLength = Name.MAX_SIZE,
            example = "Ada Lovelace")
        String name,
    @Schema(
            description = "Public username, unique and case-sensitive",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            minLength = Username.MIN_SIZE,
            maxLength = Username.MAX_SIZE,
            example = "ada_lovelace")
        String username,
    @Schema(
            description =
                "Email address; null unless the caller has "
                    + AuthPermissions.USER_READ_EMAIL
                    + " ("
                    + AuthPermissions.USER_READ_EMAIL_OWN
                    + " for themselves)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            types = {"string", "null"},
            format = "email",
            example = "ada@example.com")
        String email,
    @Schema(
            name = "isVerified",
            description = "Whether the email is verified",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "true")
        @JsonProperty("isVerified")
        boolean verified,
    @Schema(
            description =
                "Role id; null unless the caller has "
                    + AuthPermissions.USER_READ_ROLE
                    + " ("
                    + AuthPermissions.USER_READ_ROLE_OWN
                    + " for themselves), or when the user has no role",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            types = {"string", "null"},
            format = "uuid",
            example = "01936f61-8d9e-7f0a-9b1c-2d3e4f5a6b7c")
        UUID roleId,
    @Schema(
            description = "When the user was created",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-01-01T12:00:00Z")
        Instant createdAt,
    @Schema(
            description = "When the user was last changed",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-01-01T12:00:00Z")
        Instant updatedAt) {

  public static UserResponseBody from(User user) {
    return new UserResponseBody(
        user.id().value(),
        user.name().value(),
        user.username().value(),
        user.email() == null ? null : user.email().value(),
        user.verified().value(),
        user.role() == null ? null : user.role().value(),
        user.createdAt().value(),
        user.updatedAt().value());
  }
}
