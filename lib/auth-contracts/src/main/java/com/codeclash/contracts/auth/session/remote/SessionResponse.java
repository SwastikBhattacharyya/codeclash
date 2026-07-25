package com.codeclash.contracts.auth.session.remote;

import java.util.List;
import java.util.UUID;

record SessionResponse(UUID id, UUID userId, List<String> permissions) {}
