package com.codeclash.common.rest;

import com.codeclash.common.exception.CodedException;
import com.codeclash.common.exception.CommonErrorCode;
import com.codeclash.common.page.Page;
import com.codeclash.common.page.Size;
import com.codeclash.common.page.exception.PageTooSmallException;
import com.codeclash.common.page.exception.SizeTooLargeException;
import com.codeclash.common.page.exception.SizeTooSmallException;
import com.codeclash.common.rest.dto.AllowedValuesProperties;
import com.codeclash.common.rest.dto.MinProperties;
import com.codeclash.common.rest.dto.RangeProperties;
import com.codeclash.common.rest.dto.TypeMismatchProperties;
import com.codeclash.common.rest.dto.UnknownSortFieldProperties;
import com.codeclash.common.sort.Direction;
import com.codeclash.common.sort.exception.InvalidSortDirectionException;
import com.codeclash.common.sort.exception.UnknownSortFieldException;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.accept.InvalidApiVersionException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  public static final String TYPE_MISMATCH_MESSAGE = "Request value has the wrong type";
  public static final String MISSING_PARAMETER_MESSAGE = "A required request value is missing";
  public static final String MALFORMED_REQUEST_BODY_MESSAGE = "Request body could not be read";
  public static final String INTERNAL_ERROR_MESSAGE = "Something went wrong";
  public static final String INVALID_API_VERSION_MESSAGE = "This API version is not supported";
  public static final String API_VERSION_PREFIX = "v";
  public static final String SUPPORTED_API_VERSIONS_PROPERTY = "spring.mvc.apiversion.supported";

  private final List<String> allowedApiVersions;

  public GlobalExceptionHandler(Environment environment) {
    this.allowedApiVersions = GlobalExceptionHandler.allowedApiVersions(environment);
  }

  public static List<String> allowedApiVersions(Environment environment) {
    return Binder.get(environment)
        .bind(GlobalExceptionHandler.SUPPORTED_API_VERSIONS_PROPERTY, Bindable.listOf(String.class))
        .orElse(List.of())
        .stream()
        .map(version -> GlobalExceptionHandler.API_VERSION_PREFIX + version)
        .toList();
  }

  @ExceptionHandler(InvalidApiVersionException.class)
  ResponseEntity<ErrorResponseBody<AllowedValuesProperties>> handleInvalidApiVersion(
      InvalidApiVersionException exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ErrorResponseBody.of(
                CommonErrorCode.INVALID_API_VERSION,
                GlobalExceptionHandler.INVALID_API_VERSION_MESSAGE,
                new AllowedValuesProperties(this.allowedApiVersions)));
  }

  @ExceptionHandler(PageTooSmallException.class)
  ResponseEntity<ErrorResponseBody<MinProperties>> handlePageTooSmall(
      PageTooSmallException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new MinProperties(Page.MIN)));
  }

  @ExceptionHandler({SizeTooSmallException.class, SizeTooLargeException.class})
  ResponseEntity<ErrorResponseBody<RangeProperties>> handleSize(CodedException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new RangeProperties(Size.MIN, Size.MAX)));
  }

  @ExceptionHandler(UnknownSortFieldException.class)
  ResponseEntity<ErrorResponseBody<UnknownSortFieldProperties>> handleUnknownSortField(
      UnknownSortFieldException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(
            ErrorResponseBody.of(
                exception,
                new UnknownSortFieldProperties(exception.getField(), exception.getAllowed())));
  }

  @ExceptionHandler(InvalidSortDirectionException.class)
  ResponseEntity<ErrorResponseBody<AllowedValuesProperties>> handleInvalidSortDirection(
      InvalidSortDirectionException exception) {
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
        .body(ErrorResponseBody.of(exception, new AllowedValuesProperties(Direction.allowed())));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorResponseBody<Void>> handleUnexpected(Exception exception) {
    GlobalExceptionHandler.log
        .atError()
        .setMessage("request failed")
        .addKeyValue("http.status", HttpStatus.INTERNAL_SERVER_ERROR.value())
        .setCause(exception)
        .log();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(GlobalExceptionHandler.internalError());
  }

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception exception,
      Object body,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    if (status.is5xxServerError()) {
      GlobalExceptionHandler.log
          .atError()
          .setMessage("request failed")
          .addKeyValue("http.status", status.value())
          .setCause(exception)
          .log();
    }
    return super.handleExceptionInternal(
        exception, GlobalExceptionHandler.toBody(exception, status), headers, status, request);
  }

  private static ErrorResponseBody<?> toBody(Exception exception, HttpStatusCode status) {
    return switch (exception) {
      case TypeMismatchException mismatch when status.is4xxClientError() ->
          ErrorResponseBody.of(
              CommonErrorCode.TYPE_MISMATCH,
              GlobalExceptionHandler.TYPE_MISMATCH_MESSAGE,
              new TypeMismatchProperties(
                  mismatch.getPropertyName(), GlobalExceptionHandler.expectedType(mismatch)));
      case MissingRequestValueException _ when status.is4xxClientError() ->
          ErrorResponseBody.of(
              CommonErrorCode.MISSING_PARAMETER, GlobalExceptionHandler.MISSING_PARAMETER_MESSAGE);
      case HttpMessageNotReadableException _ ->
          ErrorResponseBody.of(
              CommonErrorCode.MALFORMED_REQUEST_BODY,
              GlobalExceptionHandler.MALFORMED_REQUEST_BODY_MESSAGE);
      default -> GlobalExceptionHandler.fromStatus(status);
    };
  }

  private static ErrorResponseBody<Void> fromStatus(HttpStatusCode status) {
    return switch (HttpStatus.resolve(status.value())) {
      case NOT_FOUND ->
          ErrorResponseBody.of(CommonErrorCode.NOT_FOUND, "The requested resource was not found");
      case METHOD_NOT_ALLOWED ->
          ErrorResponseBody.of(
              CommonErrorCode.METHOD_NOT_ALLOWED, "This method is not allowed for this resource");
      case NOT_ACCEPTABLE ->
          ErrorResponseBody.of(
              CommonErrorCode.NOT_ACCEPTABLE, "The requested response type is not available");
      case UNSUPPORTED_MEDIA_TYPE ->
          ErrorResponseBody.of(
              CommonErrorCode.UNSUPPORTED_MEDIA_TYPE, "This content type is not supported");
      case CONTENT_TOO_LARGE ->
          ErrorResponseBody.of(CommonErrorCode.PAYLOAD_TOO_LARGE, "Request payload is too large");
      case SERVICE_UNAVAILABLE ->
          ErrorResponseBody.of(
              CommonErrorCode.SERVICE_UNAVAILABLE, "The service is temporarily unavailable");
      case null, default ->
          status.is5xxServerError()
              ? GlobalExceptionHandler.internalError()
              : ErrorResponseBody.of(CommonErrorCode.BAD_REQUEST, "The request is not valid");
    };
  }

  private static ErrorResponseBody<Void> internalError() {
    return ErrorResponseBody.of(
        CommonErrorCode.INTERNAL_ERROR, GlobalExceptionHandler.INTERNAL_ERROR_MESSAGE);
  }

  private static String expectedType(TypeMismatchException mismatch) {
    Class<?> type = mismatch.getRequiredType();
    if (type != null
        && Collection.class.isAssignableFrom(type)
        && mismatch instanceof MethodArgumentTypeMismatchException argument) {
      type =
          ResolvableType.forMethodParameter(argument.getParameter())
              .asCollection()
              .resolveGeneric(0);
    }
    return type == null ? "unknown" : type.getSimpleName();
  }
}
