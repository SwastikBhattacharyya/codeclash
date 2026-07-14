package com.codeclash.authservice.user.port.inbound;

import com.codeclash.authservice.user.model.Id;
import com.codeclash.authservice.user.model.User;

public interface GetUserSecuredUseCase {

  User findById(Id id);
}
