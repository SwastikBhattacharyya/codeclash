package com.codeclash.problemservice.tag.model.request;

import com.codeclash.common.page.PaginationRequest;
import com.codeclash.common.sort.SortRequest;
import com.codeclash.problemservice.tag.model.Id;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record QueryTagRequest(
    List<Id> ids,
    List<String> names,
    List<String> descriptions,
    Instant minCreatedAt,
    Instant maxCreatedAt,
    Instant minUpdatedAt,
    Instant maxUpdatedAt,
    PaginationRequest pagination,
    SortRequest sort) {}
