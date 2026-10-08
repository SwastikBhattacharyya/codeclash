package com.codeclash.problemservice.question.adapter.inbound.event;

import com.codeclash.problemservice.contest.api.event.ContestOpenedEvent;
import com.codeclash.problemservice.question.model.Id;
import com.codeclash.problemservice.question.port.inbound.OpenQuestionsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContestOpenedEventListener {

  private final OpenQuestionsUseCase openQuestionsUseCase;

  @EventListener
  public void handle(ContestOpenedEvent event) {
    this.openQuestionsUseCase.open(event.questionIds().stream().map(Id::unsafe).toList());
  }
}
