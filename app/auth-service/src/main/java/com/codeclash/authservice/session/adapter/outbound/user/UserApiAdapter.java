package com.codeclash.authservice.session.adapter.outbound.user;

import com.codeclash.authservice.session.model.Credentials;
import com.codeclash.authservice.session.port.outbound.UserAuthenticator;
import com.codeclash.authservice.user.api.UserView;
import com.codeclash.authservice.user.api.usecase.AuthenticateUserApiUseCase;
import com.codeclash.authservice.user.api.usecase.FindUserRolesApiUseCase;
import com.codeclash.authservice.user.api.usecase.IsUserVerifiedApiUseCase;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserApiAdapter implements UserAuthenticator {

  private final AuthenticateUserApiUseCase authenticateUserApiUseCase;
  private final IsUserVerifiedApiUseCase isUserVerifiedApiUseCase;
  private final FindUserRolesApiUseCase findUserRolesApiUseCase;

  @Override
  public Optional<UserView> authenticate(Credentials credentials) {
    if (credentials.email() != null)
      return this.authenticateUserApiUseCase.authenticate(
          credentials.email(), credentials.password());
    return this.authenticateUserApiUseCase.authenticateByUsername(
        credentials.username(), credentials.password());
  }

  @Override
  public boolean isVerified(UUID userId) {
    return this.isUserVerifiedApiUseCase.isVerified(userId);
  }

  @Override
  public Map<UUID, UUID> findRoleIds(List<UUID> userIds) {
    return this.findUserRolesApiUseCase.findRoleIds(userIds);
  }
}
