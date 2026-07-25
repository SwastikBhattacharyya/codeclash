package com.codeclash.gatewayservice.rest.adapter.inbound.rest;

import com.codeclash.common.exception.CommonErrorCode;
import com.codeclash.common.rest.ErrorResponseBody;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class ProxyExceptionHandler {

  public static final String SERVICE_UNAVAILABLE_MESSAGE = "The upstream service is unavailable";
  public static final String GATEWAY_TIMEOUT_MESSAGE =
      "The upstream service did not respond in time";

  private static boolean isResponseTimeout(Throwable cause) {
    return cause instanceof HttpTimeoutException
        && !(cause instanceof HttpConnectTimeoutException);
  }

  private static void logFailure(ResourceAccessException exception, HttpStatus status) {
    ProxyExceptionHandler.log
        .atWarn()
        .setMessage("upstream request failed")
        .addKeyValue("http.status", status.value())
        .addKeyValue("error.cause", exception.getMostSpecificCause().toString())
        .log();
  }

  @ExceptionHandler(ResourceAccessException.class)
  ResponseEntity<ErrorResponseBody<Void>> handleResourceAccess(ResourceAccessException exception) {
    if (ProxyExceptionHandler.isResponseTimeout(exception.getCause())) {
      ProxyExceptionHandler.logFailure(exception, HttpStatus.GATEWAY_TIMEOUT);
      return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
          .body(
              ErrorResponseBody.of(
                  GatewayErrorCode.GATEWAY_TIMEOUT, ProxyExceptionHandler.GATEWAY_TIMEOUT_MESSAGE));
    }
    ProxyExceptionHandler.logFailure(exception, HttpStatus.SERVICE_UNAVAILABLE);
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(
            ErrorResponseBody.of(
                CommonErrorCode.SERVICE_UNAVAILABLE,
                ProxyExceptionHandler.SERVICE_UNAVAILABLE_MESSAGE));
  }
}
