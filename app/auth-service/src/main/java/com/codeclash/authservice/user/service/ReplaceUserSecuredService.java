package com.codeclash.authservice.user.service;

import com.codeclash.authservice.user.model.password.Raw;
import com.codeclash.authservice.user.model.request.ReplaceUserRequest;
import com.codeclash.authservice.user.model.result.ReplaceUserResult;
import com.codeclash.authservice.user.port.inbound.ReplaceUserSecuredUseCase;
import com.codeclash.authservice.user.port.inbound.ReplaceUserUseCase;
import com.codeclash.contracts.auth.session.Authenticated;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class ReplaceUserSecuredService implements ReplaceUserSecuredUseCase {

  private final ReplaceUserUseCase replaceUserUseCase;
  private final UserWriteGuard userWriteGuard;
  private final UserMasker userMasker;

  @Override
  @Authenticated
  public ReplaceUserResult replace(ReplaceUserRequest<Raw> request) {
    this.userWriteGuard.requireReplace(
        request.id(),
        request.username(),
        request.verified(),
        request.role(),
        request.verificationToken());
    UserMask mask = this.userMasker.prepare(this.userWriteGuard.isOwn(request.id()));
    ReplaceUserResult result = this.replaceUserUseCase.replace(request);
    return new ReplaceUserResult(mask.apply(result.user()), result.created());
  }
}
