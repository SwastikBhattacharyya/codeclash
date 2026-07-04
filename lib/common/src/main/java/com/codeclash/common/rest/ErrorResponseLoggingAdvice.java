package com.codeclash.common.rest;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Slf4j
@RestControllerAdvice
public class ErrorResponseLoggingAdvice implements ResponseBodyAdvice<Object> {

  private static final String UNMATCHED_ROUTE = "unmatched";

  private static String route(ServerHttpRequest request) {
    if (!(request instanceof ServletServerHttpRequest servletRequest))
      return ErrorResponseLoggingAdvice.UNMATCHED_ROUTE;
    HttpServletRequest servlet = servletRequest.getServletRequest();
    Object pattern = servlet.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    return pattern == null ? ErrorResponseLoggingAdvice.UNMATCHED_ROUTE : pattern.toString();
  }

  @Override
  public boolean supports(
      MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    return true;
  }

  @Override
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {
    if (!(body instanceof ErrorResponseBody<?> error)) return body;
    if (!(response instanceof ServletServerHttpResponse servletResponse)) return body;
    int status = servletResponse.getServletResponse().getStatus();
    if (!HttpStatusCode.valueOf(status).is4xxClientError()) return body;
    ErrorResponseLoggingAdvice.log
        .atDebug()
        .setMessage("request rejected")
        .addKeyValue("http.method", request.getMethod().name())
        .addKeyValue("http.route", ErrorResponseLoggingAdvice.route(request))
        .addKeyValue("http.status", status)
        .addKeyValue("error.code", error.code())
        .log();
    return body;
  }
}
