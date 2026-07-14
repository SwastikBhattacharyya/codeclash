package com.codeclash.authservice.user.service;

import com.codeclash.authservice.user.model.Id;
import com.codeclash.authservice.user.model.User;
import com.codeclash.authservice.user.model.exception.UserNotFoundException;
import com.codeclash.authservice.user.port.inbound.GetUserUseCase;
import com.codeclash.authservice.user.port.outbound.UserPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class GetUserService implements GetUserUseCase {

  private final UserPersistence userPersistence;

  @Override
  public User findById(Id id) {
    return this.userPersistence
        .findById(id)
        .orElseThrow(() -> new UserNotFoundException(id.value()));
  }
}
