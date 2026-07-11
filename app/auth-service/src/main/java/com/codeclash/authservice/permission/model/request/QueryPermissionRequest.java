package com.codeclash.authservice.permission.model.request;

import com.codeclash.authservice.permission.model.Id;
import com.codeclash.common.page.PaginationRequest;
import com.codeclash.common.sort.SortRequest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record QueryPermissionRequest(
    List<Id> ids,
    List<String> names,
    List<UUID> roleIds,
    Instant minCreatedAt,
    Instant maxCreatedAt,
    Instant minUpdatedAt,
    Instant maxUpdatedAt,
    PaginationRequest pagination,
    SortRequest sort) {}
