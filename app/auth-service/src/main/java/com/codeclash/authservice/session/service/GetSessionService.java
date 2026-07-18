package com.codeclash.authservice.session.service;

import com.codeclash.authservice.session.model.Id;
import com.codeclash.authservice.session.model.Session;
import com.codeclash.authservice.session.model.exception.SessionNotFoundException;
import com.codeclash.authservice.session.model.result.AuthorizedSession;
import com.codeclash.authservice.session.port.inbound.GetSessionUseCase;
import com.codeclash.authservice.session.port.outbound.SessionPersistence;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Observed
@RequiredArgsConstructor
public class GetSessionService implements GetSessionUseCase {

  private final SessionPersistence sessionPersistence;
  private final SessionAuthorizer sessionAuthorizer;

  @Override
  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public AuthorizedSession findById(Id id) {
    Session session =
        this.sessionPersistence
            .findById(id)
            .orElseThrow(() -> new SessionNotFoundException(id.value()));
    return this.sessionAuthorizer.authorize(List.of(session)).getFirst();
  }
}
