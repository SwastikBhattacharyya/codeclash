package com.codeclash.authservice.user.model;

import com.codeclash.authservice.user.model.password.Hashed;
import com.codeclash.authservice.user.model.password.Password;
import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;
import lombok.Builder;
import lombok.With;

@Builder
@With
public record User(
    Id id,
    Name name,
    Username username,
    Email email,
    Password<Hashed> password,
    Verified verified,
    VerificationToken verificationToken,
    RoleId role,
    CreatedAt createdAt,
    UpdatedAt updatedAt) {}
