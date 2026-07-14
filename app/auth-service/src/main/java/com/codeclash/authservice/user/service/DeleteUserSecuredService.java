package com.codeclash.authservice.user.service;

import com.codeclash.authservice.user.model.Id;
import com.codeclash.authservice.user.port.inbound.DeleteUserSecuredUseCase;
import com.codeclash.authservice.user.port.inbound.DeleteUserUseCase;
import com.codeclash.contracts.auth.session.Authenticated;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class DeleteUserSecuredService implements DeleteUserSecuredUseCase {

  private final DeleteUserUseCase deleteUserUseCase;
  private final UserWriteGuard userWriteGuard;

  @Override
  @Authenticated
  public void delete(Id id) {
    this.userWriteGuard.requireDelete(id);
    this.deleteUserUseCase.delete(id);
  }
}
