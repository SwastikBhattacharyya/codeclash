package com.codeclash.authservice.session.service;

import com.codeclash.authservice.session.model.Id;
import com.codeclash.authservice.session.port.inbound.DeleteSessionSecuredUseCase;
import com.codeclash.authservice.session.port.inbound.DeleteSessionUseCase;
import com.codeclash.authservice.session.port.inbound.GetSessionUseCase;
import com.codeclash.contracts.auth.AuthPermissions;
import com.codeclash.contracts.auth.session.Authenticated;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class DeleteSessionSecuredService implements DeleteSessionSecuredUseCase {

  private final DeleteSessionUseCase deleteSessionUseCase;
  private final GetSessionUseCase getSessionUseCase;
  private final SessionOwnership sessionOwnership;

  @Override
  @Authenticated
  public void delete(Id id) {
    if (!this.sessionOwnership.hasBoth(
        AuthPermissions.SESSION_WRITE, AuthPermissions.SESSION_WRITE_OWN)) {
      this.sessionOwnership.requireEither(
          AuthPermissions.SESSION_WRITE, AuthPermissions.SESSION_WRITE_OWN);
      this.sessionOwnership.requireAccess(
          this.getSessionUseCase.findById(id).session().userId().value(),
          AuthPermissions.SESSION_WRITE,
          AuthPermissions.SESSION_WRITE_OWN);
    }
    this.deleteSessionUseCase.delete(id);
  }
}
