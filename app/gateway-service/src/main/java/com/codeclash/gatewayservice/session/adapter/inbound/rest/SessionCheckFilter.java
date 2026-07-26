package com.codeclash.gatewayservice.session.adapter.inbound.rest;

import com.codeclash.common.exception.CodedException;
import com.codeclash.contracts.auth.session.BearerSessionFilter;
import com.codeclash.contracts.auth.session.SessionPrincipal;
import com.codeclash.contracts.auth.session.SessionResolver;
import com.codeclash.contracts.auth.session.exception.AuthUnavailableException;
import com.codeclash.contracts.auth.session.exception.UnauthenticatedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

@RequiredArgsConstructor
public class SessionCheckFilter extends OncePerRequestFilter {

  private static final String PREFIX = BearerSessionFilter.SCHEME + " ";

  private static final String SESSIONS_PATH = "/api/*/sessions";

  private static final String USERS_PATH = "/api/*/users";

  private static final List<PathPattern> UNCHECKED_POST_PATHS =
      List.of(
          PathPatternParser.defaultInstance.parse(SessionCheckFilter.SESSIONS_PATH),
          PathPatternParser.defaultInstance.parse(SessionCheckFilter.USERS_PATH));

  private final SessionResolver sessionResolver;

  private final HandlerExceptionResolver handlerExceptionResolver;

  private static boolean isBearer(String header) {
    return header != null
        && header.regionMatches(
            true, 0, SessionCheckFilter.PREFIX, 0, SessionCheckFilter.PREFIX.length());
  }

  private static UUID sessionId(String header) {
    try {
      return UUID.fromString(header.substring(SessionCheckFilter.PREFIX.length()).strip());
    } catch (IllegalArgumentException exception) {
      throw new UnauthenticatedException();
    }
  }

  @Override
  protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    if (!HttpMethod.POST.matches(request.getMethod())) return false;
    PathContainer path = PathContainer.parsePath(request.getRequestURI());
    return SessionCheckFilter.UNCHECKED_POST_PATHS.stream().anyMatch(pattern -> pattern.matches(path));
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (SessionCheckFilter.isBearer(header)) {
      try {
        this.check(header);
      } catch (CodedException exception) {
        this.handlerExceptionResolver.resolveException(request, response, null, exception);
        return;
      }
    }
    filterChain.doFilter(request, response);
  }

  private void check(String header) {
    UUID sessionId = SessionCheckFilter.sessionId(header);
    Optional<SessionPrincipal> principal;
    try {
      principal = this.sessionResolver.resolve(sessionId);
    } catch (AuthUnavailableException exception) {
      throw exception;
    } catch (RuntimeException exception) {
      throw new AuthUnavailableException(exception);
    }
    if (principal.isEmpty()) throw new UnauthenticatedException();
  }
}
