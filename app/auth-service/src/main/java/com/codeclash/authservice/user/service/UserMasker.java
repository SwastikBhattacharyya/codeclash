package com.codeclash.authservice.user.service;

import com.codeclash.authservice.user.model.User;
import com.codeclash.contracts.auth.AuthPermissions;
import com.codeclash.contracts.auth.session.CurrentSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class UserMasker {

  private final CurrentSession currentSession;

  UserMask prepare(boolean own) {
    return new UserMask(
        this.currentSession.hasPermission(
            own ? AuthPermissions.USER_READ_EMAIL_OWN : AuthPermissions.USER_READ_EMAIL),
        this.currentSession.hasPermission(
            own ? AuthPermissions.USER_READ_ROLE_OWN : AuthPermissions.USER_READ_ROLE));
  }

  User mask(User user) {
    boolean own =
        this.currentSession.isAuthenticated()
            && this.currentSession.userId().equals(user.id().value());
    return this.prepare(own).apply(user);
  }
}
