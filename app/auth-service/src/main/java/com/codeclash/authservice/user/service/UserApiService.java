package com.codeclash.authservice.user.service;

import com.codeclash.authservice.user.api.UserView;
import com.codeclash.authservice.user.api.usecase.AuthenticateUserApiUseCase;
import com.codeclash.authservice.user.api.usecase.FindUserRolesApiUseCase;
import com.codeclash.authservice.user.api.usecase.IsUserVerifiedApiUseCase;
import com.codeclash.authservice.user.model.Email;
import com.codeclash.authservice.user.model.Id;
import com.codeclash.authservice.user.model.User;
import com.codeclash.authservice.user.model.Username;
import com.codeclash.authservice.user.model.exception.UserNotFoundException;
import com.codeclash.authservice.user.model.password.Hashed;
import com.codeclash.authservice.user.model.password.Password;
import com.codeclash.authservice.user.model.password.Raw;
import com.codeclash.authservice.user.port.inbound.GetUserUseCase;
import com.codeclash.authservice.user.port.outbound.PasswordHasher;
import com.codeclash.authservice.user.port.outbound.UserPersistence;
import io.micrometer.observation.annotation.Observed;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
@Observed
public class UserApiService
    implements AuthenticateUserApiUseCase, IsUserVerifiedApiUseCase, FindUserRolesApiUseCase {

  private static final String DUMMY_PASSWORD = "Dummy-password-1";

  private final UserPersistence userPersistence;
  private final GetUserUseCase getUserUseCase;
  private final PasswordHasher passwordHasher;
  private final Password<Hashed> dummyHash;

  public UserApiService(
      UserPersistence userPersistence,
      GetUserUseCase getUserUseCase,
      PasswordHasher passwordHasher) {
    this.userPersistence = userPersistence;
    this.getUserUseCase = getUserUseCase;
    this.passwordHasher = passwordHasher;
    this.dummyHash = passwordHasher.hash(Password.raw(UserApiService.DUMMY_PASSWORD));
  }

  @Override
  public Optional<UserView> authenticate(String email, String password) {
    if (email == null || email.isBlank() || password == null) return Optional.empty();
    Password<Raw> candidate = Password.candidate(password);
    if (UserApiService.isTooLong(candidate)) return Optional.empty();
    return this.verify(
        this.userPersistence.findByEmail(Email.unsafe(email.strip())), candidate);
  }

  @Override
  public Optional<UserView> authenticateByUsername(String username, String password) {
    if (username == null || username.isBlank() || password == null) return Optional.empty();
    Password<Raw> candidate = Password.candidate(password);
    if (UserApiService.isTooLong(candidate)) return Optional.empty();
    return this.verify(
        this.userPersistence.findByUsername(Username.unsafe(username.strip())), candidate);
  }

  private static boolean isTooLong(Password<Raw> candidate) {
    String value = candidate.value();
    return value.codePointCount(0, value.length()) > Password.MAX_SIZE;
  }

  private Optional<UserView> verify(Optional<User> user, Password<Raw> candidate) {
    if (user.isEmpty()) {
      this.passwordHasher.matches(candidate, this.dummyHash);
      return Optional.empty();
    }
    if (!this.passwordHasher.matches(candidate, user.get().password())) return Optional.empty();
    return Optional.of(UserApiService.toView(user.get()));
  }

  @Override
  public boolean isVerified(UUID userId) {
    if (userId == null) return false;
    try {
      return this.getUserUseCase.findById(Id.unsafe(userId)).verified().value();
    } catch (UserNotFoundException exception) {
      return false;
    }
  }

  @Override
  public Map<UUID, UUID> findRoleIds(List<UUID> userIds) {
    if (userIds.isEmpty()) return Map.of();
    Map<UUID, UUID> roleIds = new HashMap<>();
    this.userPersistence
        .findRoleIds(userIds.stream().map(Id::unsafe).toList())
        .forEach((id, role) -> roleIds.put(id.value(), role.value()));
    return Map.copyOf(roleIds);
  }

  private static UserView toView(User user) {
    return new UserView(
        user.id().value(),
        user.role() == null ? null : user.role().value(),
        user.verified().value());
  }
}
