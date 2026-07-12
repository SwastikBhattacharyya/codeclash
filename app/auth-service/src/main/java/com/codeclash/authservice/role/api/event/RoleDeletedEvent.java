package com.codeclash.authservice.role.api.event;

import java.util.UUID;

public record RoleDeletedEvent(UUID roleId) {}
