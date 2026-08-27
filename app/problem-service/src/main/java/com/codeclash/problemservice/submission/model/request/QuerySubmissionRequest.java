package com.codeclash.problemservice.submission.model.request;

import com.codeclash.common.page.PaginationRequest;
import com.codeclash.common.sort.SortRequest;
import com.codeclash.contracts.problem.submission.Language;
import com.codeclash.contracts.problem.submission.SubmissionState;
import com.codeclash.problemservice.submission.model.Id;
import com.codeclash.problemservice.submission.model.QuestionId;
import com.codeclash.problemservice.submission.model.UserId;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record QuerySubmissionRequest(
    List<Id> ids,
    List<QuestionId> questionIds,
    List<UserId> userIds,
    List<String> usernames,
    List<Language> languages,
    List<SubmissionState> states,
    Boolean questionVisible,
    Instant minCreatedAt,
    Instant maxCreatedAt,
    Instant minUpdatedAt,
    Instant maxUpdatedAt,
    PaginationRequest pagination,
    SortRequest sort) {}
