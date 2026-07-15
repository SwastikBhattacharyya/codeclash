package com.codeclash.authservice.user.adapter.outbound.crypto;

import com.codeclash.authservice.user.model.password.Hashed;
import com.codeclash.authservice.user.model.password.Password;
import com.codeclash.authservice.user.model.password.Raw;
import com.codeclash.authservice.user.port.outbound.PasswordHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Argon2PasswordHasher implements PasswordHasher {

  private final PasswordEncoder passwordEncoder;

  @Override
  public Password<Hashed> hash(Password<Raw> password) {
    return Password.hashed(this.passwordEncoder.encode(password.value()));
  }

  @Override
  public boolean matches(Password<Raw> raw, Password<Hashed> hashed) {
    return this.passwordEncoder.matches(raw.value(), hashed.value());
  }
}
