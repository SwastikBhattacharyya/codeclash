package com.codeclash.common.rest;

import com.codeclash.common.exception.CodedException;
import com.codeclash.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Error returned for every failed request")
public record ErrorResponseBody<T>(
    @Schema(
            description = "Machine-readable error code",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "NAME_TOO_LONG")
        String code,
    @Schema(
            description = "Short human-readable summary of the error code",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "Name too long")
        String title,
    @Schema(
            description = "Human-readable explanation of this occurrence",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "Name must contain at most 64 characters")
        String message,
    @Schema(
            description = "When the error happened",
            requiredMode = Schema.RequiredMode.REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "2026-01-01T12:00:00Z")
        Instant timestamp,
    @Schema(
            description = "Extra details for the error code; absent when the code has none",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        T properties) {

  public static <T> ErrorResponseBody<T> of(ErrorCode errorCode, String message, T properties) {
    return new ErrorResponseBody<>(
        errorCode.name(), errorCode.title(), message, Instant.now(), properties);
  }

  public static ErrorResponseBody<Void> of(ErrorCode errorCode, String message) {
    return ErrorResponseBody.of(errorCode, message, null);
  }

  public static <T> ErrorResponseBody<T> of(CodedException exception, T properties) {
    return ErrorResponseBody.of(exception.getErrorCode(), exception.getMessage(), properties);
  }

  public static ErrorResponseBody<Void> of(CodedException exception) {
    return ErrorResponseBody.of(exception.getErrorCode(), exception.getMessage(), null);
  }
}
