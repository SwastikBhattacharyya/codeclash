package com.codeclash.authservice.user.port.inbound;

import com.codeclash.authservice.user.model.password.Raw;
import com.codeclash.authservice.user.model.request.ReplaceUserRequest;
import com.codeclash.authservice.user.model.result.ReplaceUserResult;

public interface ReplaceUserSecuredUseCase {

  ReplaceUserResult replace(ReplaceUserRequest<Raw> request);
}
