package com.codeclash.problemservice.category.adapter.inbound.rest.dto;

import com.codeclash.problemservice.category.model.Category;
import com.codeclash.problemservice.category.model.Description;
import com.codeclash.problemservice.category.model.Id;
import com.codeclash.problemservice.category.model.Name;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "A category that classifies questions")
public record CategoryResponseBody(
    @Schema(
            description = "Category id (UUID version " + Id.VERSION + ")",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "01936f3c-8b2a-7c4e-9f1d-2a3b4c5d6e7f")
        UUID id,
    @Schema(
            description = "Category name",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            minLength = Name.MIN_SIZE,
            maxLength = Name.MAX_SIZE,
            example = "graph-theory")
        String name,
    @Schema(
            description = "Category description",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            minLength = Description.MIN_SIZE,
            maxLength = Description.MAX_SIZE,
            example = "Problems on graphs and their traversal")
        String description,
    @Schema(
            description = "When the category was created",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-01-01T12:00:00Z")
        Instant createdAt,
    @Schema(
            description = "When the category was last changed",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-01-01T12:00:00Z")
        Instant updatedAt) {

  public static CategoryResponseBody from(Category category) {
    return new CategoryResponseBody(
        category.id().value(),
        category.name().value(),
        category.description().value(),
        category.createdAt().value(),
        category.updatedAt().value());
  }
}
