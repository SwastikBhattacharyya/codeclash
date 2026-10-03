package com.codeclash.contestservice.submission.model.exception;

import com.codeclash.common.exception.CodedException;

public class NullSubmissionIdException extends CodedException {

  public NullSubmissionIdException() {
    super(SubmissionErrorCode.NULL_SUBMISSION_ID, "Submission id cannot be null");
  }
}
