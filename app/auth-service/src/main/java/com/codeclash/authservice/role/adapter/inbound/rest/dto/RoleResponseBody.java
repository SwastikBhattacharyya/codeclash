package com.codeclash.authservice.role.adapter.inbound.rest.dto;

import com.codeclash.authservice.role.model.Description;
import com.codeclash.authservice.role.model.Id;
import com.codeclash.authservice.role.model.Name;
import com.codeclash.authservice.role.model.Role;
import com.codeclash.authservice.role.model.permission.PermissionId;
import com.codeclash.authservice.role.model.permission.Unresolved;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "A role and the permissions it grants")
public record RoleResponseBody(
    @Schema(
            description = "Role id (UUID version " + Id.VERSION + ")",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f61-8d9e-7f0a-9b1c-2d3e4f5a6b7c")
        UUID id,
    @Schema(
            description = "Role name",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            maxLength = Name.MAX_SIZE,
            example = "contest_manager")
        String name,
    @Schema(
            description = "What the role is for",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            minLength = Description.MIN_SIZE,
            maxLength = Description.MAX_SIZE,
            example = "Manages contests")
        String description,
    @Schema(
            name = "isDefault",
            description =
                "Whether the role is a built-in default role; default roles cannot be changed"
                    + " or deleted",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "false")
        @JsonProperty("isDefault")
        boolean isDefault,
    @ArraySchema(
            arraySchema =
                @Schema(
                    description = "Ids of the permissions the role grants",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    accessMode = Schema.AccessMode.READ_ONLY),
            schema = @Schema(format = "uuid", example = "01936f60-7c8d-7e9f-8a0b-1c2d3e4f5a6b"))
        List<UUID> permissions,
    @Schema(
            description = "When the role was created",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-01-01T12:00:00Z")
        Instant createdAt,
    @Schema(
            description =
                "When the role itself was last changed; permission changes alone do not "
                    + "update it",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-01-01T12:00:00Z")
        Instant updatedAt) {

  public static RoleResponseBody from(Role<Unresolved> role) {
    return new RoleResponseBody(
        role.id().value(),
        role.name().value(),
        role.description().value(),
        role.isDefault().value(),
        role.permissions().value().ids().stream().map(PermissionId::value).toList(),
        role.createdAt().value(),
        role.updatedAt().value());
  }
}
