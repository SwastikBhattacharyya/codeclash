package com.codeclash.authservice.permission.adapter.inbound.rest.dto;

import com.codeclash.authservice.permission.model.Id;
import com.codeclash.authservice.permission.model.Permission;
import com.codeclash.common.permission.Description;
import com.codeclash.common.permission.Name;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "A permission that roles can grant")
public record PermissionResponseBody(
    @Schema(
            description = "Permission id (UUID version " + Id.VERSION + ")",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f60-7c8d-7e9f-8a0b-1c2d3e4f5a6b")
        UUID id,
    @Schema(
            description = "Permission name in service:resource:action form",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            maxLength = Name.MAX_SIZE,
            example = "problem:tag:write")
        String name,
    @Schema(
            description = "What the permission allows",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            minLength = Description.MIN_SIZE,
            maxLength = Description.MAX_SIZE,
            example = "Create, replace, patch and delete tags")
        String description,
    @Schema(
            description = "When the permission was created",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-01-01T12:00:00Z")
        Instant createdAt,
    @Schema(
            description = "When the permission was last changed",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-01-01T12:00:00Z")
        Instant updatedAt) {

  public static PermissionResponseBody from(Permission permission) {
    return new PermissionResponseBody(
        permission.id().value(),
        permission.name().value(),
        permission.description().value(),
        permission.createdAt().value(),
        permission.updatedAt().value());
  }
}
