package com.codeclash.problemservice.category.adapter.inbound.rest.dto;

import com.codeclash.common.patch.PatchObject;
import com.codeclash.problemservice.category.model.Description;
import com.codeclash.problemservice.category.model.Id;
import com.codeclash.problemservice.category.model.Name;
import com.codeclash.problemservice.category.model.request.PatchCategoryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.openapitools.jackson.nullable.JsonNullable;

@Schema(description = "Partial update of a category; only the fields sent are changed")
public record PatchCategoryRequestBody(
    @Schema(
            description =
                "Category name on a single line; surrounding whitespace is trimmed;"
                    + " leave out to keep the current name",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = String.class,
            minLength = Name.MIN_SIZE,
            maxLength = Name.MAX_SIZE,
            example = "graph-theory")
        JsonNullable<String> name,
    @Schema(
            description =
                "Category description; surrounding whitespace is trimmed;"
                    + " leave out to keep the current description",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = String.class,
            minLength = Description.MIN_SIZE,
            maxLength = Description.MAX_SIZE,
            example = "Problems on graphs and their traversal")
        JsonNullable<String> description) {

  public PatchCategoryRequest toRequest(UUID id) {
    return new PatchCategoryRequest(
        Id.of(id),
        PatchObject.from(this.name).map(Name::of),
        PatchObject.from(this.description).map(Description::of));
  }
}
