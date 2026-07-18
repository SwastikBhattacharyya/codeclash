package com.codeclash.authservice.session.model;

import com.codeclash.authservice.session.model.exception.AmbiguousCredentialsException;
import com.codeclash.authservice.session.model.exception.NullCredentialsException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public final class Credentials {

  private final String email;
  private final String username;
  private final String password;

  private Credentials(String email, String username, String password) {
    this.email = email;
    this.username = username;
    this.password = password;
  }

  public static Credentials of(String email, String username, String password) {
    if (password == null || (email == null && username == null))
      throw new NullCredentialsException();
    if (email != null && username != null) throw new AmbiguousCredentialsException();
    return new Credentials(email, username, password);
  }

  public String email() {
    return this.email;
  }

  public String username() {
    return this.username;
  }

  public String password() {
    return this.password;
  }
}
