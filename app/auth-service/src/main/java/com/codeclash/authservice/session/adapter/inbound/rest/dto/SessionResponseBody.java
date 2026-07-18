package com.codeclash.authservice.session.adapter.inbound.rest.dto;

import com.codeclash.authservice.session.model.Id;
import com.codeclash.authservice.session.model.result.AuthorizedSession;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "A login session; its id is the bearer token")
public record SessionResponseBody(
    @Schema(
            description =
                "Session id (UUID version "
                    + Id.VERSION
                    + "); send it as Authorization: Bearer <id>",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f70-9e0f-7a1b-8c2d-3e4f5a6b7c8d")
        UUID id,
    @Schema(
            description = "Id of the user the session belongs to",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f71-0f1a-7b2c-9d3e-4f5a6b7c8d9e")
        UUID userId,
    @Schema(
            description = "Name of the user's current role; null if the user has no role",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            types = {"string", "null"},
            example = "user")
        String roleName,
    @ArraySchema(
            arraySchema =
                @Schema(
                    description =
                        "Permissions of the user's current role, sorted; empty if the user has no"
                            + " role",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    accessMode = Schema.AccessMode.READ_ONLY),
            schema = @Schema(example = "auth:session:read_own"))
        List<String> permissions,
    @Schema(
            description = "When the session was last used to authenticate a request",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-01-01T12:00:00Z")
        Instant lastUsedAt,
    @Schema(
            description = "When the session was created (login time)",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-01-01T12:00:00Z")
        Instant createdAt,
    @Schema(
            description = "When the session was last changed",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-01-01T12:00:00Z")
        Instant updatedAt) {

  public static SessionResponseBody from(AuthorizedSession authorized) {
    return new SessionResponseBody(
        authorized.session().id().value(),
        authorized.session().userId().value(),
        authorized.roleName(),
        authorized.permissions(),
        authorized.session().lastUsedAt().value(),
        authorized.session().createdAt().value(),
        authorized.session().updatedAt().value());
  }
}
