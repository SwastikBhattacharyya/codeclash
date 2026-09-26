package com.codeclash.contestservice.contest.service;

import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.model.exception.ContestNotFoundException;
import com.codeclash.contestservice.contest.model.question.ContestQuestions;
import com.codeclash.contestservice.contest.model.request.PatchContestRequest;
import com.codeclash.contestservice.contest.model.result.ContestWriteResult;
import com.codeclash.contestservice.contest.port.inbound.PatchContestUseCase;
import com.codeclash.contestservice.contest.port.inbound.RebuildLeaderboardUseCase;
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
public class PatchContestService implements PatchContestUseCase {

  private final ContestPersistence contestPersistence;
  private final ContestScheduleRules contestScheduleRules;
  private final ContestEventPublisher contestEventPublisher;
  private final RebuildLeaderboardUseCase rebuildLeaderboardUseCase;

  @Override
  @Transactional
  public Contest patch(PatchContestRequest request) {
    ContestWriteResult written =
        this.contestPersistence
            .patch(request)
            .orElseThrow(() -> new ContestNotFoundException(request.id().value()));
    Contest current = written.current();
    ContestQuestions questions = request.questions().orElse(current.questions());
    this.contestScheduleRules.requireWritable(
        written.previous(), current.liveAt(), current.endAt(), questions);
    if (!written.previous().questions().equals(questions))
      this.contestPersistence.replaceQuestions(request.id(), questions);
    Contest patched = current.withQuestions(questions);
    if (!written.previous().endAt().equals(current.endAt())
        && this.contestScheduleRules.isStarted(written.previous()))
      this.rebuildLeaderboardUseCase.rebuild(List.of(current.id()));
    this.contestEventPublisher.written(patched);
    PatchContestService.log
        .atInfo()
        .setMessage("contest patched")
        .addKeyValue("contest.id", patched.id().value())
        .log();
    return patched;
  }
}
