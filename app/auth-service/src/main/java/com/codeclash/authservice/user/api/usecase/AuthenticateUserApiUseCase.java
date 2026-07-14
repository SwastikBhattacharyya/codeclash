package com.codeclash.authservice.user.api.usecase;

import com.codeclash.authservice.user.api.UserView;
import java.util.Optional;

public interface AuthenticateUserApiUseCase {

  Optional<UserView> authenticate(String email, String password);

  Optional<UserView> authenticateByUsername(String username, String password);
}
