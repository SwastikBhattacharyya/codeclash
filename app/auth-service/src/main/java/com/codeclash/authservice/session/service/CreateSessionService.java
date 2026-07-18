package com.codeclash.authservice.session.service;

import com.codeclash.authservice.session.model.Session;
import com.codeclash.authservice.session.model.UserId;
import com.codeclash.authservice.session.model.exception.InvalidCredentialsException;
import com.codeclash.authservice.session.model.exception.UnverifiedUserException;
import com.codeclash.authservice.session.model.request.CreateSessionRequest;
import com.codeclash.authservice.session.model.result.AuthorizedSession;
import com.codeclash.authservice.session.port.inbound.CreateSessionUseCase;
import com.codeclash.authservice.session.port.outbound.SessionPersistence;
import com.codeclash.authservice.session.port.outbound.UserAuthenticator;
import com.codeclash.authservice.user.api.UserView;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class CreateSessionService implements CreateSessionUseCase {

  private final SessionPersistence sessionPersistence;
  private final UserAuthenticator userAuthenticator;
  private final SessionAuthorizer sessionAuthorizer;

  @Override
  public AuthorizedSession create(CreateSessionRequest request) {
    UserView user =
        this.userAuthenticator
            .authenticate(request.credentials())
            .orElseThrow(InvalidCredentialsException::new);
    if (!this.userAuthenticator.isVerified(user.id())) throw new UnverifiedUserException();
    Session session = this.sessionPersistence.create(UserId.unsafe(user.id()));
    CreateSessionService.log
        .atInfo()
        .setMessage("session created")
        .addKeyValue("user.id", user.id())
        .log();
    return this.sessionAuthorizer.authorize(List.of(session)).getFirst();
  }
}
