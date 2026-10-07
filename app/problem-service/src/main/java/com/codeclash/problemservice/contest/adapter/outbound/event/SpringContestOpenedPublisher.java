package com.codeclash.problemservice.contest.adapter.outbound.event;

import com.codeclash.problemservice.contest.api.event.ContestOpenedEvent;
import com.codeclash.problemservice.contest.model.Contest;
import com.codeclash.problemservice.contest.port.outbound.ContestOpenedPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringContestOpenedPublisher implements ContestOpenedPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  public void opened(Contest contest) {
    this.applicationEventPublisher.publishEvent(
        new ContestOpenedEvent(contest.id().value(), contest.questionIds()));
  }
}
