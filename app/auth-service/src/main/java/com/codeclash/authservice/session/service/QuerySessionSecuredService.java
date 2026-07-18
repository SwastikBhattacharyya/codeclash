package com.codeclash.authservice.session.service;

import com.codeclash.authservice.session.model.UserId;
import com.codeclash.authservice.session.model.request.QuerySessionRequest;
import com.codeclash.authservice.session.model.result.AuthorizedSession;
import com.codeclash.authservice.session.port.inbound.QuerySessionSecuredUseCase;
import com.codeclash.authservice.session.port.inbound.QuerySessionUseCase;
import com.codeclash.common.page.PagedEntity;
import com.codeclash.contracts.auth.AuthPermissions;
import com.codeclash.contracts.auth.session.Authenticated;
import com.codeclash.contracts.auth.session.CurrentSession;
import com.codeclash.contracts.auth.session.exception.ForbiddenException;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class QuerySessionSecuredService implements QuerySessionSecuredUseCase {

  private final QuerySessionUseCase querySessionUseCase;
  private final CurrentSession currentSession;

  @Override
  @Authenticated
  public PagedEntity<AuthorizedSession> query(QuerySessionRequest request) {
    if (!this.currentSession.hasPermission(AuthPermissions.SESSION_READ)) {
      UUID userId = this.currentSession.userId();
      boolean own =
          !request.userIds().isEmpty()
              && request.userIds().stream().map(UserId::value).allMatch(userId::equals);
      if (!own) throw new ForbiddenException(List.of(AuthPermissions.SESSION_READ));
      if (!this.currentSession.hasPermission(AuthPermissions.SESSION_READ_OWN))
        throw new ForbiddenException(List.of(AuthPermissions.SESSION_READ_OWN));
    }
    return this.querySessionUseCase.query(request);
  }
}
