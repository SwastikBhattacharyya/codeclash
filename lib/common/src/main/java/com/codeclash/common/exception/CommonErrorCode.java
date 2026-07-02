package com.codeclash.common.exception;

public enum CommonErrorCode implements ErrorCode {
  PAGE_TOO_SMALL("Page too small"),
  SIZE_TOO_SMALL("Size too small"),
  SIZE_TOO_LARGE("Size too large"),
  INVALID_SORT_DIRECTION("Invalid sort direction"),
  UNKNOWN_SORT_FIELD("Unknown sort field"),
  NOT_FOUND("Not found"),
  METHOD_NOT_ALLOWED("Method not allowed"),
  NOT_ACCEPTABLE("Not acceptable"),
  UNSUPPORTED_MEDIA_TYPE("Unsupported media type"),
  TYPE_MISMATCH("Type mismatch"),
  INVALID_API_VERSION("Invalid API version"),
  MISSING_PARAMETER("Missing parameter"),
  MALFORMED_REQUEST_BODY("Malformed request body"),
  BAD_REQUEST("Bad request"),
  PAYLOAD_TOO_LARGE("Payload too large"),
  SERVICE_UNAVAILABLE("Service unavailable"),
  UNSUPPORTED_DATABASE("Unsupported database"),
  INTERNAL_ERROR("Internal error");

  private final String title;

  CommonErrorCode(String title) {
    this.title = title;
  }

  @Override
  public String title() {
    return this.title;
  }
}
