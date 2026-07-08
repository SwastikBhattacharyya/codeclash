package com.codeclash.contracts.auth.session;

import java.util.Set;
import java.util.UUID;

public record SessionPrincipal(UUID userId, Set<String> permissions) {

  public SessionPrincipal {
    permissions = Set.copyOf(permissions);
  }
}
