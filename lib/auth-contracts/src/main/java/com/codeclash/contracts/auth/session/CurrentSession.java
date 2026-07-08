package com.codeclash.contracts.auth.session;

import com.codeclash.contracts.auth.session.exception.AuthUnavailableException;
import com.codeclash.contracts.auth.session.exception.ForbiddenException;
import com.codeclash.contracts.auth.session.exception.UnauthenticatedException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public class CurrentSession {

  public boolean isAuthenticated() {
    return SessionContext.get() instanceof SessionState.Authenticated;
  }

  public void requireAuthenticated() {
    this.principal();
  }

  public UUID userId() {
    return this.principal().userId();
  }

  public boolean hasPermission(String permission) {
    return this.permissions().contains(permission);
  }

  public void requireAllPermissions(String... permissions) {
    Set<String> granted = this.principal().permissions();
    List<String> missing =
        Stream.of(permissions).filter(permission -> !granted.contains(permission)).toList();
    if (!missing.isEmpty()) throw new ForbiddenException(missing);
  }

  SessionPrincipal principal() {
    return switch (SessionContext.get()) {
      case SessionState.Authenticated authenticated -> authenticated.principal();
      case SessionState.Anonymous anonymous -> throw new UnauthenticatedException();
      case SessionState.Unavailable unavailable -> throw new AuthUnavailableException();
    };
  }

  private Set<String> permissions() {
    return switch (SessionContext.get()) {
      case SessionState.Authenticated authenticated -> authenticated.principal().permissions();
      case SessionState.Anonymous anonymous -> Set.of();
      case SessionState.Unavailable unavailable -> throw new AuthUnavailableException();
    };
  }
}
