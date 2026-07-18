package com.codeclash.authservice.session.model.request;

import com.codeclash.authservice.session.model.Id;
import com.codeclash.authservice.session.model.UserId;
import com.codeclash.common.page.PaginationRequest;
import com.codeclash.common.sort.SortRequest;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record QuerySessionRequest(
    List<Id> ids,
    List<UserId> userIds,
    Instant minLastUsedAt,
    Instant maxLastUsedAt,
    Instant minCreatedAt,
    Instant maxCreatedAt,
    Instant minUpdatedAt,
    Instant maxUpdatedAt,
    PaginationRequest pagination,
    SortRequest sort) {}
