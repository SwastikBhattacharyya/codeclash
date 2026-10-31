package com.codeclash.runnerservice.submission.adapter.inbound.kafka;

import com.codeclash.runnerservice.submission.adapter.inbound.worker.WorkSignal;
import com.codeclash.runnerservice.submission.model.Code;
import com.codeclash.runnerservice.submission.model.Id;
import com.codeclash.runnerservice.submission.model.QuestionId;
import com.codeclash.runnerservice.submission.model.request.EnqueueSubmissionRequest;
import com.codeclash.runnerservice.submission.port.inbound.CancelSubmissionUseCase;
import com.codeclash.runnerservice.submission.port.inbound.EnqueueSubmissionUseCase;
import com.codeclash.common.event.CdcEvent;
import com.codeclash.contracts.problem.event.SubmissionCdcEventData;
import com.codeclash.contracts.problem.submission.SubmissionState;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubmissionCdcListener {

  private final EnqueueSubmissionUseCase enqueueSubmissionUseCase;
  private final CancelSubmissionUseCase cancelSubmissionUseCase;
  private final WorkSignal workSignal;

  private static EnqueueSubmissionRequest toRequest(SubmissionCdcEventData data) {
    return new EnqueueSubmissionRequest(
        Id.of(data.id()), data.language(), new Code(data.code()), QuestionId.of(data.questionId()));
  }

  @KafkaListener(
      topics = "${codeclash.submission.cdc-consumer.topic}",
      containerFactory = "submissionCdcListenerContainerFactory")
  public void handle(CdcEvent<SubmissionCdcEventData> event) {
    SubmissionCdcEventData data = event.data();
    switch (event.operation()) {
      case WRITE -> {
        if (data.state() != SubmissionState.QUEUED) return;
        this.enqueueSubmissionUseCase.enqueue(SubmissionCdcListener.toRequest(data));
        this.workSignal.signal();
      }
      case DELETE -> this.cancelSubmissionUseCase.cancel(Id.of(data.id()));
    }
  }
}
