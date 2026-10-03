package com.codeclash.contestservice.submission.port.outbound;

import com.codeclash.contestservice.submission.model.Id;
import com.codeclash.contestservice.submission.model.Submission;
import com.codeclash.contestservice.submission.model.result.SyncSubmissionResult;
import java.util.Optional;

public interface SubmissionPersistence {

  Optional<SyncSubmissionResult> upsert(Submission submission);

  Optional<Submission> deleteById(Id id);
}
