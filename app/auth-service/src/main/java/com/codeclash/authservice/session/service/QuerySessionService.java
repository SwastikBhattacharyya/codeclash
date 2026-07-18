package com.codeclash.authservice.session.service;

import com.codeclash.authservice.session.model.Session;
import com.codeclash.authservice.session.model.request.QuerySessionRequest;
import com.codeclash.authservice.session.model.result.AuthorizedSession;
import com.codeclash.authservice.session.port.inbound.QuerySessionUseCase;
import com.codeclash.authservice.session.port.outbound.SessionPersistence;
import com.codeclash.common.page.PagedEntity;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Observed
@RequiredArgsConstructor
public class QuerySessionService implements QuerySessionUseCase {

  private final SessionPersistence sessionPersistence;
  private final SessionAuthorizer sessionAuthorizer;

  @Override
  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public PagedEntity<AuthorizedSession> query(QuerySessionRequest request) {
    PagedEntity<Session> page = this.sessionPersistence.query(request);
    return new PagedEntity<>(
        this.sessionAuthorizer.authorize(page.data()),
        page.page(),
        page.size(),
        page.items(),
        page.next());
  }
}
