package com.codeclash.authservice.user.adapter.inbound.rest.dto;

import com.codeclash.authservice.user.model.Email;
import com.codeclash.authservice.user.model.Id;
import com.codeclash.authservice.user.model.Name;
import com.codeclash.authservice.user.model.RoleId;
import com.codeclash.authservice.user.model.Username;
import com.codeclash.authservice.user.model.VerificationToken;
import com.codeclash.authservice.user.model.Verified;
import com.codeclash.authservice.user.model.password.Password;
import com.codeclash.authservice.user.model.password.Raw;
import com.codeclash.authservice.user.model.request.ReplaceUserRequest;
import com.codeclash.contracts.auth.AuthPermissions;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(
    description =
        "Full replacement of a user; omitted verified and roleId take their defaults, and the"
            + " user is created when the id does not exist")
public record ReplaceUserRequestBody(
    @Schema(
            description =
                "Display name with at least two ASCII letters; surrounding whitespace is "
                    + "trimmed",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            minLength = Name.MIN_SIZE,
            maxLength = Name.MAX_SIZE,
            example = "Ada Lovelace")
        String name,
    @Schema(
            description =
                "Public username, unique and case-sensitive, using only the characters "
                    + Username.ALLOWED_CHARACTERS
                    + "; surrounding whitespace is trimmed; changing it needs the "
                    + AuthPermissions.USER_WRITE_USERNAME
                    + " permission (_own for yourself)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            minLength = Username.MIN_SIZE,
            maxLength = Username.MAX_SIZE,
            example = "ada_lovelace")
        String username,
    @Schema(
            description = "Email address, unique ignoring case; surrounding whitespace is trimmed",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            maxLength = Email.MAX_SIZE,
            format = "email",
            example = "ada@example.com")
        String email,
    @Schema(
            name = "password",
            description =
                "Password with a lowercase letter, an uppercase letter, a digit and a special"
                    + " character",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            minLength = Password.MIN_SIZE,
            maxLength = Password.MAX_SIZE,
            format = "password",
            example = "Str0ng!pass")
        @JsonProperty("password")
        String rawPassword,
    @Schema(
            description =
                "Whether the email is verified; changing it needs the "
                    + AuthPermissions.USER_WRITE_IS_VERIFIED
                    + " permission (_own for yourself); omitted means false",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            defaultValue = "false",
            example = "false")
        Boolean verified,
    @Schema(
            description =
                "Role id; changing it needs the "
                    + AuthPermissions.USER_WRITE_ROLE
                    + " permission (_own for yourself) plus every permission of the new role; "
                    + "omitted means the default user role",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            format = "uuid",
            example = "01936f61-8d9e-7f0a-9b1c-2d3e4f5a6b7c")
        UUID roleId) {

  public ReplaceUserRequest<Raw> toRequest(UUID id, UUID verificationToken) {
    return new ReplaceUserRequest<>(
        Id.of(id),
        Name.of(this.name),
        Username.of(this.username),
        Email.of(this.email),
        Password.raw(this.rawPassword),
        this.verified == null ? null : new Verified(this.verified),
        this.roleId == null ? null : RoleId.of(this.roleId),
        verificationToken == null ? null : VerificationToken.of(verificationToken));
  }
}
