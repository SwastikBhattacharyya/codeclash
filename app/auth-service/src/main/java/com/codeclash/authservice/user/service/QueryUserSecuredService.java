package com.codeclash.authservice.user.service;

import com.codeclash.authservice.user.model.User;
import com.codeclash.authservice.user.model.request.QueryUserRequest;
import com.codeclash.authservice.user.port.inbound.QueryUserSecuredUseCase;
import com.codeclash.authservice.user.port.inbound.QueryUserUseCase;
import com.codeclash.common.page.PagedEntity;
import com.codeclash.contracts.auth.AuthPermissions;
import com.codeclash.contracts.auth.session.CurrentSession;
import com.codeclash.contracts.auth.session.exception.ForbiddenException;
import io.micrometer.observation.annotation.Observed;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class QueryUserSecuredService implements QueryUserSecuredUseCase {

  private final QueryUserUseCase queryUserUseCase;
  private final UserMasker userMasker;
  private final CurrentSession currentSession;

  @Override
  public PagedEntity<User> query(QueryUserRequest request) {
    List<String> missing = new ArrayList<>();
    if ((!request.emails().isEmpty() || "email".equals(request.sort().field()))
        && !this.currentSession.hasPermission(AuthPermissions.USER_READ_EMAIL))
      missing.add(AuthPermissions.USER_READ_EMAIL);
    if (!request.roleIds().isEmpty()
        && !this.currentSession.hasPermission(AuthPermissions.USER_READ_ROLE))
      missing.add(AuthPermissions.USER_READ_ROLE);
    if (!missing.isEmpty()) throw new ForbiddenException(missing);
    return this.queryUserUseCase.query(request).map(this.userMasker::mask);
  }
}
