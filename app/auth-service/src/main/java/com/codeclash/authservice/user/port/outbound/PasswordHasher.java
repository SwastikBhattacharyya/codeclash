package com.codeclash.authservice.user.port.outbound;

import com.codeclash.authservice.user.model.password.Hashed;
import com.codeclash.authservice.user.model.password.Password;
import com.codeclash.authservice.user.model.password.Raw;

public interface PasswordHasher {

  Password<Hashed> hash(Password<Raw> password);

  boolean matches(Password<Raw> raw, Password<Hashed> hashed);
}
