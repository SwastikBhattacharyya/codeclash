package com.codeclash.authservice.user.service;

import com.codeclash.authservice.user.model.Id;
import com.codeclash.authservice.user.model.User;
import com.codeclash.authservice.user.port.inbound.GetUserSecuredUseCase;
import com.codeclash.authservice.user.port.inbound.GetUserUseCase;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class GetUserSecuredService implements GetUserSecuredUseCase {

  private final GetUserUseCase getUserUseCase;
  private final UserMasker userMasker;

  @Override
  public User findById(Id id) {
    return this.userMasker.mask(this.getUserUseCase.findById(id));
  }
}
