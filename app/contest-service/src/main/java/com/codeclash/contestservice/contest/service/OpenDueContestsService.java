package com.codeclash.contestservice.contest.service;

import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.port.inbound.OpenDueContestsUseCase;
import com.codeclash.contestservice.contest.port.outbound.ContestEventPublisher;
import com.codeclash.contestservice.contest.port.outbound.ContestPersistence;
import io.micrometer.observation.annotation.Observed;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class OpenDueContestsService implements OpenDueContestsUseCase {

  private final ContestPersistence contestPersistence;
  private final ContestEventPublisher contestEventPublisher;

  @Override
  @Transactional
  public List<Contest> openDue(Instant cutoff, int limit) {
    List<Contest> opened = this.contestPersistence.openDue(cutoff, limit);
    opened.forEach(this.contestEventPublisher::written);
    if (!opened.isEmpty())
      OpenDueContestsService.log
          .atInfo()
          .setMessage("contests opened")
          .addKeyValue("count", opened.size())
          .log();
    return opened;
  }
}
