package com.codeclash.authservice.user.port.inbound;

import com.codeclash.authservice.user.model.User;
import com.codeclash.authservice.user.model.password.Raw;
import com.codeclash.authservice.user.model.request.PatchUserRequest;

public interface PatchUserUseCase {

  User patch(PatchUserRequest<Raw> request);
}
