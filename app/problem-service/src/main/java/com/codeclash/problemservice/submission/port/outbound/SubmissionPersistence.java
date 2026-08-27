package com.codeclash.problemservice.submission.port.outbound;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.contracts.problem.submission.SubmissionState;
import com.codeclash.problemservice.submission.model.Id;
import com.codeclash.problemservice.submission.model.Submission;
import com.codeclash.problemservice.submission.model.owner.Owned;
import com.codeclash.problemservice.submission.model.request.ApplySubmissionStateRequest;
import com.codeclash.problemservice.submission.model.request.CreateSubmissionRequest;
import com.codeclash.problemservice.submission.model.request.QuerySubmissionRequest;
import java.util.List;
import java.util.Optional;

public interface SubmissionPersistence {

  PagedEntity<Submission> query(QuerySubmissionRequest request);

  Optional<Submission> findById(Id id);

  Submission create(CreateSubmissionRequest<Owned> request, SubmissionState state);

  Optional<Submission> deleteById(Id id);

  Optional<Submission> applyState(
      ApplySubmissionStateRequest request, List<SubmissionState> predecessors);
}
