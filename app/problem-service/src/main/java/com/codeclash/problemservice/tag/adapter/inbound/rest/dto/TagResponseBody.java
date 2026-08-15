package com.codeclash.problemservice.tag.adapter.inbound.rest.dto;

import com.codeclash.problemservice.tag.model.Description;
import com.codeclash.problemservice.tag.model.Id;
import com.codeclash.problemservice.tag.model.Name;
import com.codeclash.problemservice.tag.model.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "A tag that groups related questions")
public record TagResponseBody(
    @Schema(
            description = "Tag id (UUID version " + Id.VERSION + ")",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
        UUID id,
    @Schema(
            description = "Tag name",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            minLength = Name.MIN_SIZE,
            maxLength = Name.MAX_SIZE,
            example = "dynamic-programming")
        String name,
    @Schema(
            description = "Tag description",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            minLength = Description.MIN_SIZE,
            maxLength = Description.MAX_SIZE,
            example = "Problems solved with dynamic programming")
        String description,
    @Schema(
            description = "When the tag was created",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-01-01T12:00:00Z")
        Instant createdAt,
    @Schema(
            description = "When the tag was last changed",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-01-01T12:00:00Z")
        Instant updatedAt) {

  public static TagResponseBody from(Tag tag) {
    return new TagResponseBody(
        tag.id().value(),
        tag.name().value(),
        tag.description().value(),
        tag.createdAt().value(),
        tag.updatedAt().value());
  }
}
