package com.codeclash.problemservice.testcase.model.request;

import com.codeclash.common.page.PaginationRequest;
import com.codeclash.common.sort.SortRequest;
import com.codeclash.problemservice.testcase.model.Id;
import com.codeclash.problemservice.testcase.model.QuestionId;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record QueryTestCaseRequest(
    QuestionId questionId,
    List<Id> ids,
    Boolean isPublic,
    Instant minCreatedAt,
    Instant maxCreatedAt,
    Instant minUpdatedAt,
    Instant maxUpdatedAt,
    PaginationRequest pagination,
    SortRequest sort) {}
