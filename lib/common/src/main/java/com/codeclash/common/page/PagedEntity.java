package com.codeclash.common.page;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.function.Function;

@Schema(description = "One page of results")
public record PagedEntity<T>(
    @ArraySchema(
            arraySchema =
                @Schema(
                    description = "Items on this page",
                    requiredMode = Schema.RequiredMode.REQUIRED,
                    accessMode = Schema.AccessMode.READ_ONLY))
        List<T> data,
    @Schema(
            description = "Page number, starting at " + Page.MIN,
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            minimum = "" + Page.MIN,
            example = Page.DEFAULT)
        int page,
    @Schema(
            description = "Maximum number of items per page",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            minimum = "" + Size.MIN,
            maximum = "" + Size.MAX,
            example = Size.DEFAULT)
        int size,
    @Schema(
            description = "Total number of matching items across all pages",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "42")
        int items,
    @Schema(
            name = "hasNext",
            description = "Whether another page follows this one",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "true")
        @JsonProperty("hasNext")
        boolean next) {

  public <R> PagedEntity<R> map(Function<T, R> mapper) {
    return new PagedEntity<>(
        this.data.stream().map(mapper).toList(), this.page, this.size, this.items, this.next);
  }
}
