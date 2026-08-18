package com.codeclash.problemservice.question.model.request;

import com.codeclash.common.page.PaginationRequest;
import com.codeclash.common.sort.SortRequest;
import com.codeclash.problemservice.question.model.Id;
import com.codeclash.problemservice.question.model.category.CategoryId;
import com.codeclash.problemservice.question.model.tag.TagId;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record QueryQuestionRequest(
    List<Id> ids,
    List<String> titles,
    List<String> summaries,
    List<String> descriptions,
    Boolean visible,
    List<CategoryId> categoryIds,
    List<TagId> tagIds,
    Instant minCreatedAt,
    Instant maxCreatedAt,
    Instant minUpdatedAt,
    Instant maxUpdatedAt,
    PaginationRequest pagination,
    SortRequest sort) {}
