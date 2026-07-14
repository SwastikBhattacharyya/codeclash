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
import com.codeclash.common.patch.PatchObject;

public record PatchUserRequest<P extends PasswordState>(
    Id id,
    PatchObject<Name> name,
    PatchObject<Username> username,
    PatchObject<Email> email,
    PatchObject<Password<P>> password,
    PatchObject<Verified> verified,
    PatchObject<RoleId> role,
    VerificationToken verificationToken) {

  public <T extends PasswordState> PatchUserRequest<T> withPassword(
      PatchObject<Password<T>> password) {
    return new PatchUserRequest<>(
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
