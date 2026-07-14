package com.codeclash.authservice.user.service;

import com.codeclash.authservice.user.model.Id;
import com.codeclash.authservice.user.model.User;
import com.codeclash.authservice.user.model.Verified;
import com.codeclash.authservice.user.model.exception.UserNotFoundException;
import com.codeclash.authservice.user.model.exception.VerificationTokenMismatchException;
import com.codeclash.authservice.user.model.password.Raw;
import com.codeclash.authservice.user.model.request.PatchUserRequest;
import com.codeclash.authservice.user.port.inbound.GetUserUseCase;
import com.codeclash.authservice.user.port.inbound.PatchUserSecuredUseCase;
import com.codeclash.authservice.user.port.inbound.PatchUserUseCase;
import com.codeclash.contracts.auth.session.CurrentSession;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class PatchUserSecuredService implements PatchUserSecuredUseCase {

  private final PatchUserUseCase patchUserUseCase;
  private final UserWriteGuard userWriteGuard;
  private final UserMasker userMasker;
  private final CurrentSession currentSession;
  private final GetUserUseCase getUserUseCase;

  private static boolean isAnonymousVerification(PatchUserRequest<Raw> request) {
    Verified verified = request.verified().orElse(null);
    return request.verificationToken() != null
        && verified != null
        && verified.value()
        && !request.name().isPresent()
        && !request.username().isPresent()
        && !request.email().isPresent()
        && !request.password().isPresent()
        && !request.role().isPresent();
  }

  private void requireUnverified(Id id) {
    try {
      if (this.getUserUseCase.findById(id).verified().value())
        throw new VerificationTokenMismatchException();
    } catch (UserNotFoundException exception) {
      throw new VerificationTokenMismatchException();
    }
  }

  @Override
  public User patch(PatchUserRequest<Raw> request) {
    if (!this.currentSession.isAuthenticated()
        && PatchUserSecuredService.isAnonymousVerification(request)) {
      this.requireUnverified(request.id());
      UserMask mask = this.userMasker.prepare(false);
      return mask.apply(this.patchUserUseCase.patch(request));
    }
    this.currentSession.requireAuthenticated();
    this.userWriteGuard.requirePatch(
        request.id(),
        request.username(),
        request.verified(),
        request.role(),
        request.verificationToken());
    UserMask mask = this.userMasker.prepare(this.userWriteGuard.isOwn(request.id()));
    return mask.apply(this.patchUserUseCase.patch(request));
  }
}
