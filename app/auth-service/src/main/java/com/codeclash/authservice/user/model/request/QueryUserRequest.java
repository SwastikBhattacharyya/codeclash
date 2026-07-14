package com.codeclash.authservice.user.model.request;

import com.codeclash.authservice.user.model.Id;
import com.codeclash.authservice.user.model.RoleId;
import com.codeclash.common.page.PaginationRequest;
import com.codeclash.common.sort.SortRequest;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record QueryUserRequest(
    List<Id> ids,
    List<String> names,
    List<String> usernames,
    List<String> emails,
    List<RoleId> roleIds,
    Boolean verified,
    Instant minCreatedAt,
    Instant maxCreatedAt,
    Instant minUpdatedAt,
    Instant maxUpdatedAt,
    PaginationRequest pagination,
    SortRequest sort) {}
