package com.codeclash.authservice.session.service;

import com.codeclash.authservice.session.model.Id;
import com.codeclash.authservice.session.model.result.AuthorizedSession;
import com.codeclash.authservice.session.port.inbound.GetSessionSecuredUseCase;
import com.codeclash.authservice.session.port.inbound.GetSessionUseCase;
import com.codeclash.contracts.auth.AuthPermissions;
import com.codeclash.contracts.auth.session.Authenticated;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class GetSessionSecuredService implements GetSessionSecuredUseCase {

  private final GetSessionUseCase getSessionUseCase;
  private final SessionOwnership sessionOwnership;

  @Override
  @Authenticated
  public AuthorizedSession findById(Id id) {
    this.sessionOwnership.requireEither(
        AuthPermissions.SESSION_READ, AuthPermissions.SESSION_READ_OWN);
    AuthorizedSession session = this.getSessionUseCase.findById(id);
    this.sessionOwnership.requireAccess(
        session.session().userId().value(),
        AuthPermissions.SESSION_READ,
        AuthPermissions.SESSION_READ_OWN);
    return session;
  }
}
