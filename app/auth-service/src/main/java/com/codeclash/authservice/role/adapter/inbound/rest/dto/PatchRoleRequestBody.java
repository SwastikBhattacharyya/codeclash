package com.codeclash.authservice.role.adapter.inbound.rest.dto;

import com.codeclash.authservice.role.model.Description;
import com.codeclash.authservice.role.model.Id;
import com.codeclash.authservice.role.model.Name;
import com.codeclash.authservice.role.model.permission.Permissions;
import com.codeclash.authservice.role.model.permission.Unresolved;
import com.codeclash.authservice.role.model.request.PatchRoleRequest;
import com.codeclash.common.patch.PatchObject;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import org.openapitools.jackson.nullable.JsonNullable;

@Schema(description = "Partial update of a role; only the fields sent are changed")
public record PatchRoleRequestBody(
    @Schema(
            description =
                "Role name: lowercase letters in words joined by single underscores; "
                    + "surrounding whitespace is trimmed; leave out to keep the current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = String.class,
            maxLength = Name.MAX_SIZE,
            example = "contest_manager")
        JsonNullable<String> name,
    @Schema(
            description =
                "What the role is for; surrounding whitespace is trimmed; leave out to keep "
                    + "the current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = String.class,
            minLength = Description.MIN_SIZE,
            maxLength = Description.MAX_SIZE,
            example = "Manages contests")
        JsonNullable<String> description,
    @Schema(
            description =
                "Ids of the permissions the role grants; replaces the whole set, duplicates "
                    + "are ignored, [] for none; leave out to keep the current value",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = UUID[].class,
            example = "[\"01936f60-7c8d-7e9f-8a0b-1c2d3e4f5a6b\"]")
        JsonNullable<List<UUID>> permissions) {

  public PatchRoleRequest<Unresolved> toRequest(UUID id) {
    return new PatchRoleRequest<>(
        Id.of(id),
        PatchObject.from(this.name).map(Name::of),
        PatchObject.from(this.description).map(Description::of),
        PatchObject.from(this.permissions).map(Permissions::unresolved));
  }
}
