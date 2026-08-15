package com.codeclash.problemservice.tag.adapter.inbound.rest.dto;

import com.codeclash.common.patch.PatchObject;
import com.codeclash.problemservice.tag.model.Description;
import com.codeclash.problemservice.tag.model.Id;
import com.codeclash.problemservice.tag.model.Name;
import com.codeclash.problemservice.tag.model.request.PatchTagRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import org.openapitools.jackson.nullable.JsonNullable;

@Schema(description = "Partial update of a tag; only the fields sent are changed")
public record PatchTagRequestBody(
    @Schema(
            description =
                "Tag name on a single line; surrounding whitespace is trimmed;"
                    + " leave out to keep the current name",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = String.class,
            minLength = Name.MIN_SIZE,
            maxLength = Name.MAX_SIZE,
            example = "dynamic-programming")
        JsonNullable<String> name,
    @Schema(
            description =
                "Tag description; surrounding whitespace is trimmed;"
                    + " leave out to keep the current description",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.WRITE_ONLY,
            implementation = String.class,
            minLength = Description.MIN_SIZE,
            maxLength = Description.MAX_SIZE,
            example = "Problems solved with dynamic programming")
        JsonNullable<String> description) {

  public PatchTagRequest toRequest(UUID id) {
    return new PatchTagRequest(
        Id.of(id),
        PatchObject.from(this.name).map(Name::of),
        PatchObject.from(this.description).map(Description::of));
  }
}
