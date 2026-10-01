package com.codeclash.contestservice.contest.adapter.inbound.scheduler;

import com.codeclash.contestservice.contest.adapter.config.ContestProperties;
import com.codeclash.contestservice.contest.port.inbound.OpenDueContestsUseCase;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContestOpenerScheduler {

  private final OpenDueContestsUseCase openDueContestsUseCase;
  private final ContestProperties contestProperties;

  @Scheduled(fixedDelayString = "${codeclash.contest.opener.interval}")
  public void open() {
    Instant cutoff = Instant.now().plus(this.contestProperties.opener().lead());
    int batchSize = this.contestProperties.opener().batchSize();
    int opened;
    do opened = this.openDueContestsUseCase.openDue(cutoff, batchSize).size();
    while (opened == batchSize);
  }
}
