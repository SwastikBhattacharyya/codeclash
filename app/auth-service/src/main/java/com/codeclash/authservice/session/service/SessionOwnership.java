package com.codeclash.authservice.session.service;

import com.codeclash.contracts.auth.session.CurrentSession;
import com.codeclash.contracts.auth.session.exception.ForbiddenException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class SessionOwnership {

  private final CurrentSession currentSession;

  boolean hasBoth(String other, String own) {
    return this.currentSession.hasPermission(other) && this.currentSession.hasPermission(own);
  }

  void requireEither(String other, String own) {
    if (!this.currentSession.hasPermission(other) && !this.currentSession.hasPermission(own))
      throw new ForbiddenException(List.of(other, own));
  }

  void requireAccess(UUID ownerId, String other, String own) {
    String permission = this.currentSession.userId().equals(ownerId) ? own : other;
    if (!this.currentSession.hasPermission(permission))
      throw new ForbiddenException(List.of(permission));
  }
}
