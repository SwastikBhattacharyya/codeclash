package com.codeclash.contestservice.contest.service;

import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.model.request.ReplaceContestRequest;
import com.codeclash.contestservice.contest.model.result.ContestWriteResult;
import com.codeclash.contestservice.contest.model.result.ReplaceContestResult;
import com.codeclash.contestservice.contest.port.inbound.RebuildLeaderboardUseCase;
import com.codeclash.contestservice.contest.port.inbound.ReplaceContestUseCase;
import com.codeclash.contestservice.contest.port.outbound.ContestEventPublisher;
import com.codeclash.contestservice.contest.port.outbound.ContestPersistence;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class ReplaceContestService implements ReplaceContestUseCase {

  private final ContestPersistence contestPersistence;
  private final ContestScheduleRules contestScheduleRules;
  private final ContestEventPublisher contestEventPublisher;
  private final RebuildLeaderboardUseCase rebuildLeaderboardUseCase;

  @Override
  @Transactional
  public ReplaceContestResult replace(ReplaceContestRequest request) {
    ContestWriteResult written = this.contestPersistence.replace(request);
    this.contestScheduleRules.requireWritable(
        written.previous(), request.liveAt(), request.endAt(), request.questions());
    if (written.previous() == null || !written.previous().questions().equals(request.questions()))
      this.contestPersistence.replaceQuestions(request.id(), request.questions());
    Contest current = written.current().withQuestions(request.questions());
    if (this.endAtMovedAfterStart(written.previous(), current))
      this.rebuildLeaderboardUseCase.rebuild(List.of(current.id()));
    this.contestEventPublisher.written(current);
    ReplaceContestService.log
        .atInfo()
        .setMessage("contest replaced")
        .addKeyValue("contest.id", current.id().value())
        .addKeyValue("created", written.previous() == null)
        .log();
    return new ReplaceContestResult(current, written.previous() == null);
  }

  private boolean endAtMovedAfterStart(Contest previous, Contest current) {
    return previous != null
        && !previous.endAt().equals(current.endAt())
        && this.contestScheduleRules.isStarted(previous);
  }
}
