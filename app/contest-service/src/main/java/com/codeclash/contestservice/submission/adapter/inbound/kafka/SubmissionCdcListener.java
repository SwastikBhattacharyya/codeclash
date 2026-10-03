package com.codeclash.contestservice.submission.adapter.inbound.kafka;

import com.codeclash.common.event.CdcEvent;
import com.codeclash.common.timestamp.CreatedAt;
import com.codeclash.common.timestamp.UpdatedAt;
import com.codeclash.contestservice.submission.model.Id;
import com.codeclash.contestservice.submission.model.Submission;
import com.codeclash.contestservice.submission.port.inbound.SyncSubmissionUseCase;
import com.codeclash.contracts.problem.event.SubmissionCdcEventData;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubmissionCdcListener {

  private final SyncSubmissionUseCase syncSubmissionUseCase;

  private static Submission toSubmission(SubmissionCdcEventData data) {
    return new Submission(
        Id.of(data.id()),
        data.questionId(),
        data.userId(),
        data.state(),
        new CreatedAt(data.createdAt()),
        new UpdatedAt(data.updatedAt()));
  }

  @KafkaListener(
      topics = "${codeclash.submission.cdc-consumer.topic}",
      containerFactory = "submissionCdcListenerContainerFactory")
  public void handle(CdcEvent<SubmissionCdcEventData> event) {
    SubmissionCdcEventData data = event.data();
    switch (event.operation()) {
      case WRITE -> this.syncSubmissionUseCase.write(SubmissionCdcListener.toSubmission(data));
      case DELETE -> this.syncSubmissionUseCase.delete(Id.of(data.id()));
    }
  }
}
