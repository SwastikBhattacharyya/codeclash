package com.codeclash.contestservice.contest.model.request;

import com.codeclash.common.page.PaginationRequest;
import com.codeclash.common.sort.SortRequest;
import com.codeclash.contestservice.contest.model.Id;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record QueryContestRequest(
    List<Id> ids,
    List<String> names,
    List<String> descriptions,
    Instant minLiveAt,
    Instant maxLiveAt,
    Instant minEndAt,
    Instant maxEndAt,
    Instant minCreatedAt,
    Instant maxCreatedAt,
    Instant minUpdatedAt,
    Instant maxUpdatedAt,
    PaginationRequest pagination,
    SortRequest sort) {}
