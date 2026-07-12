package com.codeclash.authservice.role.adapter.inbound.rest.dto;

import com.codeclash.authservice.role.model.Description;
import com.codeclash.authservice.role.model.Name;
import com.codeclash.authservice.role.model.permission.Permissions;
import com.codeclash.authservice.role.model.permission.Unresolved;
import com.codeclash.authservice.role.model.request.CreateRoleRequest;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "Role to create; new roles are never default roles")
public record CreateRoleRequestBody(
    @Schema(
            description =
                "Role name: lowercase letters in words joined by single underscores; "
                    + "surrounding whitespace is trimmed",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            maxLength = Name.MAX_SIZE,
            example = "contest_manager")
        String name,
    @Schema(
            description = "What the role is for; surrounding whitespace is trimmed",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            minLength = Description.MIN_SIZE,
            maxLength = Description.MAX_SIZE,
            example = "Manages contests")
        String description,
    @ArraySchema(
            arraySchema =
                @Schema(
                    description =
                        "Ids of the permissions the role grants; replaces the whole set, "
                            + "duplicates are ignored, [] for none",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    accessMode = Schema.AccessMode.WRITE_ONLY),
            schema = @Schema(format = "uuid", example = "01936f60-7c8d-7e9f-8a0b-1c2d3e4f5a6b"))
        List<UUID> permissions) {

  public CreateRoleRequest<Unresolved> toRequest() {
    return new CreateRoleRequest<>(
        Name.of(this.name),
        Description.of(this.description),
        Permissions.unresolved(this.permissions));
  }
}
