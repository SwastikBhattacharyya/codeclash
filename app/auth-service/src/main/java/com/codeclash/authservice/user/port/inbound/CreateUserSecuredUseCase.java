package com.codeclash.authservice.user.port.inbound;

import com.codeclash.authservice.user.model.User;
import com.codeclash.authservice.user.model.password.Raw;
import com.codeclash.authservice.user.model.request.CreateUserRequest;

public interface CreateUserSecuredUseCase {

  User create(CreateUserRequest<Raw> request);
}
