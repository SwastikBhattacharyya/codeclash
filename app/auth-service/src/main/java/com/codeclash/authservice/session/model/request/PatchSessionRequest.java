package com.codeclash.authservice.session.model.request;

import com.codeclash.authservice.session.model.Id;
import com.codeclash.authservice.session.model.LastUsedAt;
import com.codeclash.authservice.session.model.UserId;
import com.codeclash.common.patch.PatchObject;

public record PatchSessionRequest(
    Id id, PatchObject<UserId> userId, PatchObject<LastUsedAt> lastUsedAt) {}
