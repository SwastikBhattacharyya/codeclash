package com.codeclash.problemservice.category.model.request;

import com.codeclash.common.page.PaginationRequest;
import com.codeclash.common.sort.SortRequest;
import com.codeclash.problemservice.category.model.Id;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record QueryCategoryRequest(
    List<Id> ids,
    List<String> names,
    List<String> descriptions,
    Instant minCreatedAt,
    Instant maxCreatedAt,
    Instant minUpdatedAt,
    Instant maxUpdatedAt,
    PaginationRequest pagination,
    SortRequest sort) {}
