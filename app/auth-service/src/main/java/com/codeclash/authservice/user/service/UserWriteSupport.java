package com.codeclash.authservice.user.service;

import com.codeclash.authservice.user.model.RoleId;
import com.codeclash.authservice.user.model.User;
import com.codeclash.authservice.user.model.VerificationToken;
import com.codeclash.authservice.user.model.Verified;
import com.codeclash.authservice.user.model.exception.VerificationTokenMismatchException;
import com.codeclash.authservice.user.port.inbound.GetDefaultRoleUseCase;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class UserWriteSupport {

  private final GetDefaultRoleUseCase getDefaultRoleUseCase;

  static Verified verifiedOrDefault(Verified verified) {
    return verified != null ? verified : new Verified(false);
  }

  static void requireTokenMatch(
      Optional<User> stored, Verified verified, VerificationToken token) {
    if (token == null || verified == null || !verified.value()) return;
    if (stored.isPresent() && stored.get().verified().value()) return;
    if (stored.isEmpty() || !token.equals(stored.get().verificationToken()))
      throw new VerificationTokenMismatchException();
  }

  RoleId roleOrDefault(RoleId role) {
    return role != null ? role : this.getDefaultRoleUseCase.get();
  }
}
