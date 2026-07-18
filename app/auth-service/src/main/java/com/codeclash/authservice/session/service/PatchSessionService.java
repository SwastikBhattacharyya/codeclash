package com.codeclash.authservice.session.service;

import com.codeclash.authservice.session.model.Session;
import com.codeclash.authservice.session.model.exception.SessionNotFoundException;
import com.codeclash.authservice.session.model.request.PatchSessionRequest;
import com.codeclash.authservice.session.model.result.AuthorizedSession;
import com.codeclash.authservice.session.port.inbound.PatchSessionUseCase;
import com.codeclash.authservice.session.port.outbound.SessionPersistence;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class PatchSessionService implements PatchSessionUseCase {

  private final SessionPersistence sessionPersistence;
  private final SessionAuthorizer sessionAuthorizer;

  @Override
  public AuthorizedSession patch(PatchSessionRequest request) {
    Session session =
        this.sessionPersistence
            .patch(request)
            .orElseThrow(() -> new SessionNotFoundException(request.id().value()));
    PatchSessionService.log
        .atInfo()
        .setMessage("session patched")
        .addKeyValue("user.id", session.userId().value())
        .log();
    return this.sessionAuthorizer.authorize(List.of(session)).getFirst();
  }
}
