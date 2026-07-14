package com.codeclash.authservice.user.service;

import com.codeclash.authservice.user.model.Id;
import com.codeclash.authservice.user.model.RoleId;
import com.codeclash.authservice.user.model.User;
import com.codeclash.authservice.user.model.Username;
import com.codeclash.authservice.user.model.VerificationToken;
import com.codeclash.authservice.user.model.Verified;
import com.codeclash.authservice.user.model.exception.UserNotFoundException;
import com.codeclash.authservice.user.port.inbound.GetUserUseCase;
import com.codeclash.authservice.user.port.outbound.RoleLookup;
import com.codeclash.common.patch.PatchObject;
import com.codeclash.contracts.auth.AuthPermissions;
import com.codeclash.contracts.auth.session.CurrentSession;
import com.codeclash.contracts.auth.session.exception.ForbiddenException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class UserWriteGuard {

  private final CurrentSession currentSession;
  private final GetUserUseCase getUserUseCase;
  private final UserWriteSupport userWriteSupport;
  private final RoleLookup roleLookup;

  boolean isOwn(Id id) {
    return this.currentSession.isAuthenticated()
        && this.currentSession.userId().equals(id.value());
  }

  void requireCreate(Verified verified, RoleId role) {
    Set<String> missing = new LinkedHashSet<>();
    this.addFields(
        missing,
        false,
        false,
        this.userWriteSupport.roleOrDefault(null),
        UserWriteSupport.verifiedOrDefault(verified),
        this.userWriteSupport.roleOrDefault(role),
        null,
        null,
        null);
    UserWriteGuard.throwIfMissing(missing);
  }

  void requireReplace(
      Id id, Username username, Verified verified, RoleId role, VerificationToken token) {
    boolean self = this.isOwn(id);
    this.requireWrite(self);
    Optional<User> stored = this.findStored(id);
    boolean own = self && stored.isPresent();
    if (self && !own) this.requireWrite(false);
    Set<String> missing = new LinkedHashSet<>();
    stored
        .filter(user -> !own)
        .ifPresent(user -> missing.addAll(this.missingFrom(user.role())));
    this.addFields(
        missing,
        own,
        stored.map(user -> user.verified().value()).orElse(false),
        stored.isPresent() ? stored.get().role() : this.userWriteSupport.roleOrDefault(null),
        UserWriteSupport.verifiedOrDefault(verified),
        this.userWriteSupport.roleOrDefault(role),
        token,
        stored.map(User::username).orElse(null),
        username);
    UserWriteGuard.throwIfMissing(missing);
  }

  void requirePatch(
      Id id,
      PatchObject<Username> username,
      PatchObject<Verified> verified,
      PatchObject<RoleId> role,
      VerificationToken token) {
    boolean own = this.isOwn(id);
    this.requireWrite(own);
    if (own && !username.isPresent() && !verified.isPresent() && !role.isPresent()) return;
    User stored = this.getUserUseCase.findById(id);
    Set<String> missing = new LinkedHashSet<>();
    if (!own) missing.addAll(this.missingFrom(stored.role()));
    this.addFields(
        missing,
        own,
        stored.verified().value(),
        stored.role(),
        verified.orElse(null),
        role.isPresent() ? role.orElse(null) : stored.role(),
        token,
        stored.username(),
        username.orElse(null));
    UserWriteGuard.throwIfMissing(missing);
  }

  void requireDelete(Id id) {
    boolean own = this.isOwn(id);
    this.requireWrite(own);
    if (own) return;
    UserWriteGuard.throwIfMissing(this.missingFrom(this.getUserUseCase.findById(id).role()));
  }

  private void requireWrite(boolean own) {
    String permission = own ? AuthPermissions.USER_WRITE_OWN : AuthPermissions.USER_WRITE;
    if (!this.currentSession.hasPermission(permission))
      throw new ForbiddenException(List.of(permission));
  }

  private void addFields(
      Set<String> missing,
      boolean own,
      boolean baselineVerified,
      RoleId baselineRole,
      Verified verified,
      RoleId role,
      VerificationToken token,
      Username baselineUsername,
      Username username) {
    if (baselineUsername != null && username != null && !username.equals(baselineUsername)) {
      String permission =
          own ? AuthPermissions.USER_WRITE_USERNAME_OWN : AuthPermissions.USER_WRITE_USERNAME;
      if (!this.currentSession.hasPermission(permission)) missing.add(permission);
    }
    boolean verifying = verified != null && verified.value() && !baselineVerified;
    if (verified != null
        && verified.value() != baselineVerified
        && !(verifying && token != null)) {
      String permission =
          own ? AuthPermissions.USER_WRITE_IS_VERIFIED_OWN : AuthPermissions.USER_WRITE_IS_VERIFIED;
      if (!this.currentSession.hasPermission(permission)) missing.add(permission);
    }
    if (!Objects.equals(role, baselineRole)) {
      String permission =
          own ? AuthPermissions.USER_WRITE_ROLE_OWN : AuthPermissions.USER_WRITE_ROLE;
      if (!this.currentSession.hasPermission(permission)) missing.add(permission);
      missing.addAll(this.missingFrom(role));
    }
  }

  private Set<String> missingFrom(RoleId role) {
    Set<String> missing = new LinkedHashSet<>();
    for (String permission : this.roleLookup.findPermissions(role))
      if (!this.currentSession.hasPermission(permission)) missing.add(permission);
    return missing;
  }

  private Optional<User> findStored(Id id) {
    try {
      return Optional.of(this.getUserUseCase.findById(id));
    } catch (UserNotFoundException exception) {
      return Optional.empty();
    }
  }

  private static void throwIfMissing(Set<String> missing) {
    if (!missing.isEmpty()) throw new ForbiddenException(List.copyOf(missing));
  }
}
