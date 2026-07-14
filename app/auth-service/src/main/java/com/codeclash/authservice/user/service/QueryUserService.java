package com.codeclash.authservice.user.service;

import com.codeclash.authservice.user.model.User;
import com.codeclash.authservice.user.model.request.QueryUserRequest;
import com.codeclash.authservice.user.port.inbound.QueryUserUseCase;
import com.codeclash.authservice.user.port.outbound.UserPersistence;
import com.codeclash.common.page.PagedEntity;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Observed
@RequiredArgsConstructor
public class QueryUserService implements QueryUserUseCase {

  private final UserPersistence userPersistence;

  @Override
  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public PagedEntity<User> query(QueryUserRequest request) {
    return this.userPersistence.query(request);
  }
}
