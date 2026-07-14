package com.codeclash.authservice.user.model.password;

public sealed interface PasswordState permits Raw, Hashed {}
