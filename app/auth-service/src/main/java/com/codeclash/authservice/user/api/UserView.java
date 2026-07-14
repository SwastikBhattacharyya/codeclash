package com.codeclash.authservice.user.api;

import java.util.UUID;

public record UserView(UUID id, UUID roleId, boolean verified) {}
