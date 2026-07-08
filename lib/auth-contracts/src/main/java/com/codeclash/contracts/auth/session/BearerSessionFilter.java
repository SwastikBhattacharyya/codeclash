package com.codeclash.contracts.auth.session;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class BearerSessionFilter extends OncePerRequestFilter {

  public static final String SCHEME = "Bearer";

  private static final String PREFIX = BearerSessionFilter.SCHEME + " ";

  private final SessionResolver sessionResolver;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    SessionContext.set(this.resolve(request.getHeader(HttpHeaders.AUTHORIZATION)));
    try {
      filterChain.doFilter(request, response);
    } finally {
      SessionContext.clear();
    }
  }

  private SessionState resolve(String header) {
    if (header == null
        || !header.regionMatches(
            true, 0, BearerSessionFilter.PREFIX, 0, BearerSessionFilter.PREFIX.length()))
      return new SessionState.Anonymous();
    UUID sessionId;
    try {
      sessionId = UUID.fromString(header.substring(BearerSessionFilter.PREFIX.length()).strip());
    } catch (IllegalArgumentException exception) {
      return new SessionState.Anonymous();
    }
    try {
      return this.sessionResolver
          .resolve(sessionId)
          .<SessionState>map(SessionState.Authenticated::new)
          .orElseGet(SessionState.Anonymous::new);
    } catch (RuntimeException exception) {
      return new SessionState.Unavailable();
    }
  }
}
