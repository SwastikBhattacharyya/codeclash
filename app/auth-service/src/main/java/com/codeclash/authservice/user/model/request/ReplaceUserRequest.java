package com.codeclash.authservice.user.model.request;

import com.codeclash.authservice.user.model.Email;
import com.codeclash.authservice.user.model.Id;
import com.codeclash.authservice.user.model.Name;
import com.codeclash.authservice.user.model.RoleId;
import com.codeclash.authservice.user.model.Username;
import com.codeclash.authservice.user.model.VerificationToken;
import com.codeclash.authservice.user.model.Verified;
import com.codeclash.authservice.user.model.password.Password;
import com.codeclash.authservice.user.model.password.PasswordState;
import lombok.With;

public record ReplaceUserRequest<P extends PasswordState>(
    Id id,
    Name name,
    Username username,
    Email email,
    Password<P> password,
    @With Verified verified,
    @With RoleId role,
    VerificationToken verificationToken) {

  public <T extends PasswordState> ReplaceUserRequest<T> withPassword(Password<T> password) {
    return new ReplaceUserRequest<>(
        this.id,
        this.name,
        this.username,
        this.email,
        password,
        this.verified,
        this.role,
        this.verificationToken);
  }
}
