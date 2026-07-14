package com.codeclash.authservice.user.port.inbound;

import com.codeclash.authservice.user.model.User;
import com.codeclash.authservice.user.model.request.QueryUserRequest;
import com.codeclash.common.page.PagedEntity;

public interface QueryUserUseCase {

  PagedEntity<User> query(QueryUserRequest request);
}
