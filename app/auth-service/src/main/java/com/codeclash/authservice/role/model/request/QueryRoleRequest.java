package com.codeclash.authservice.role.model.request;

import com.codeclash.authservice.role.model.Id;
import com.codeclash.authservice.role.model.permission.PermissionId;
import com.codeclash.common.page.PaginationRequest;
import com.codeclash.common.sort.SortRequest;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record QueryRoleRequest(
    List<Id> ids,
    List<String> names,
    List<PermissionId> permissionIds,
    Boolean isDefault,
    Instant minCreatedAt,
    Instant maxCreatedAt,
    Instant minUpdatedAt,
    Instant maxUpdatedAt,
    PaginationRequest pagination,
    SortRequest sort) {}
