package com.codeclash.problemservice.submission.adapter.inbound.kafka;

import com.codeclash.contracts.runner.event.SubmissionStateEventData;
import com.codeclash.problemservice.submission.model.ActualOutput;
import com.codeclash.problemservice.submission.model.CompileOutput;
import com.codeclash.problemservice.submission.model.FailedTestCaseId;
import com.codeclash.problemservice.submission.model.Id;
import com.codeclash.problemservice.submission.model.MemoryUsed;
import com.codeclash.problemservice.submission.model.TimeTaken;
import com.codeclash.problemservice.submission.model.exception.InvalidSubmissionStateException;
import com.codeclash.problemservice.submission.model.request.ApplySubmissionStateRequest;
import com.codeclash.problemservice.submission.port.inbound.ApplySubmissionStateUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubmissionStateListener {

  private final ApplySubmissionStateUseCase applySubmissionStateUseCase;

  private static ApplySubmissionStateRequest toRequest(SubmissionStateEventData data) {
    if (data.state() == null) throw new InvalidSubmissionStateException(null);
    return new ApplySubmissionStateRequest(
        Id.of(data.submissionId()),
        data.state(),
        data.failedTestCaseId() == null ? null : FailedTestCaseId.of(data.failedTestCaseId()),
        data.timeTakenMs() == null ? null : new TimeTaken(data.timeTakenMs()),
        data.memoryUsedKb() == null ? null : new MemoryUsed(data.memoryUsedKb()),
        data.compileOutput() == null ? null : CompileOutput.of(data.compileOutput()),
        data.actualOutput() == null ? null : ActualOutput.of(data.actualOutput()));
  }

  @KafkaListener(
      topics = "${codeclash.submission.state-consumer.topic}",
      containerFactory = "submissionStateListenerContainerFactory")
  public void handle(SubmissionStateEventData event) {
    this.applySubmissionStateUseCase.apply(SubmissionStateListener.toRequest(event));
  }
}
