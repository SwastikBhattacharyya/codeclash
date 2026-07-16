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
import com.codeclash.authservice.user.model.request.PatchUserRequest;
import com.codeclash.common.patch.PatchObject;
import com.codeclash.contracts.auth.AuthPermissions;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.openapitools.jackson.nullable.JsonNullable;

@Schema(description = "Partial update of a user; only the fields sent are changed")
public record PatchUserRequestBody(
    @Schema(
            description =
                "Display name with at least two ASCII letters; surrounding whitespace is "
                    + "trimmed; leave out to keep the current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = String.class,
            minLength = Name.MIN_SIZE,
            maxLength = Name.MAX_SIZE,
            example = "Ada Lovelace")
        JsonNullable<String> name,
    @Schema(
            description =
                "Public username, unique and case-sensitive, using only the characters "
                    + Username.ALLOWED_CHARACTERS
                    + "; surrounding whitespace is trimmed; changing it needs the "
                    + AuthPermissions.USER_WRITE_USERNAME
                    + " permission (_own for yourself); leave out to keep the current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = String.class,
            minLength = Username.MIN_SIZE,
            maxLength = Username.MAX_SIZE,
            example = "ada_lovelace")
        JsonNullable<String> username,
    @Schema(
            description =
                "Email address, unique ignoring case; surrounding whitespace is trimmed; "
                    + "leave out to keep the current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = String.class,
            maxLength = Email.MAX_SIZE,
            format = "email",
            example = "ada@example.com")
        JsonNullable<String> email,
    @Schema(
            name = "password",
            description =
                "Password with a lowercase letter, an uppercase letter, a digit and a special"
                    + " character; leave out to keep the current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = String.class,
            minLength = Password.MIN_SIZE,
            maxLength = Password.MAX_SIZE,
            format = "password",
            example = "Str0ng!pass")
        @JsonProperty("password")
        JsonNullable<String> rawPassword,
    @Schema(
            description =
                "Whether the email is verified; changing it needs the "
                    + AuthPermissions.USER_WRITE_IS_VERIFIED
                    + " permission (_own for yourself); leave out to keep the current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = Boolean.class,
            example = "false")
        JsonNullable<Boolean> verified,
    @Schema(
            description =
                "Role id; changing it needs the "
                    + AuthPermissions.USER_WRITE_ROLE
                    + " permission (_own for yourself) plus every permission of the new role; "
                    + "leave out to keep the current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = UUID.class,
            format = "uuid",
            example = "01936f61-8d9e-7f0a-9b1c-2d3e4f5a6b7c")
        JsonNullable<UUID> roleId) {

  public PatchUserRequest<Raw> toRequest(UUID id, UUID verificationToken) {
    return new PatchUserRequest<>(
        Id.of(id),
        PatchObject.from(this.name).map(Name::of),
        PatchObject.from(this.username).map(Username::of),
        PatchObject.from(this.email).map(Email::of),
        PatchObject.from(this.rawPassword).map(Password::raw),
        PatchObject.from(this.verified).map(Verified::new),
        PatchObject.from(this.roleId).map(RoleId::of),
        verificationToken == null ? null : VerificationToken.of(verificationToken));
  }
}
