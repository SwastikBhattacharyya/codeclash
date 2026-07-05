package com.codeclash.common.openapi;

import com.codeclash.common.exception.CodedException;
import com.codeclash.common.exception.ErrorCode;
import com.codeclash.common.rest.ErrorResponseBody;
import io.swagger.v3.oas.models.examples.Example;
import java.time.Instant;

public final class ApiExamples {

  public static final String REF_PREFIX = "#/components/examples/";
  public static final String NAMESPACE_SEPARATOR = ".";
  public static final Instant TIMESTAMP = Instant.parse("2026-01-01T12:00:00Z");

  public static final String TYPE_MISMATCH = "common.TYPE_MISMATCH";
  public static final String INVALID_API_VERSION = "common.INVALID_API_VERSION";
  public static final String MISSING_PARAMETER = "common.MISSING_PARAMETER";
  public static final String MALFORMED_REQUEST_BODY = "common.MALFORMED_REQUEST_BODY";
  public static final String INTERNAL_ERROR = "common.INTERNAL_ERROR";
  public static final String PAGE_TOO_SMALL = "common.PAGE_TOO_SMALL";
  public static final String SIZE_TOO_SMALL = "common.SIZE_TOO_SMALL";
  public static final String SIZE_TOO_LARGE = "common.SIZE_TOO_LARGE";
  public static final String UNKNOWN_SORT_FIELD = "common.UNKNOWN_SORT_FIELD";
  public static final String INVALID_SORT_DIRECTION = "common.INVALID_SORT_DIRECTION";

  private ApiExamples() {}

  public static Example error(ErrorCode errorCode, String message, Object properties) {
    return new Example()
        .summary(errorCode.name())
        .value(
            new ErrorResponseBody<>(
                errorCode.name(),
                errorCode.title(),
                message,
                ApiExamples.TIMESTAMP,
                properties));
  }

  public static Example error(ErrorCode errorCode, String message) {
    return ApiExamples.error(errorCode, message, null);
  }

  public static Example error(CodedException exception, Object properties) {
    return ApiExamples.error(exception.getErrorCode(), exception.getMessage(), properties);
  }

  public static Example error(CodedException exception) {
    return ApiExamples.error(exception.getErrorCode(), exception.getMessage(), null);
  }

  public static String code(String name) {
    return name.substring(
        name.indexOf(ApiExamples.NAMESPACE_SEPARATOR) + ApiExamples.NAMESPACE_SEPARATOR.length());
  }

  public static Example ref(String name) {
    return new Example().$ref(ApiExamples.REF_PREFIX + name);
  }
}
