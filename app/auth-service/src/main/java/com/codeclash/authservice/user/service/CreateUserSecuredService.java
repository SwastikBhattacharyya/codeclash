package com.codeclash.authservice.user.service;

import com.codeclash.authservice.user.model.User;
import com.codeclash.authservice.user.model.password.Raw;
import com.codeclash.authservice.user.model.request.CreateUserRequest;
import com.codeclash.authservice.user.port.inbound.CreateUserSecuredUseCase;
import com.codeclash.authservice.user.port.inbound.CreateUserUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class CreateUserSecuredService implements CreateUserSecuredUseCase {

  private final CreateUserUseCase createUserUseCase;
  private final UserWriteGuard userWriteGuard;
  private final UserMasker userMasker;

  @Override
  public User create(CreateUserRequest<Raw> request) {
    this.userWriteGuard.requireCreate(request.verified(), request.role());
    UserMask mask = this.userMasker.prepare(false);
    return mask.apply(this.createUserUseCase.create(request));
  }
}
