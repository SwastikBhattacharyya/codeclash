package com.codeclash.contestservice.contest.service;

import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.model.EndAt;
import com.codeclash.contestservice.contest.model.LiveAt;
import com.codeclash.contestservice.contest.model.exception.ContestAlreadyLiveException;
import com.codeclash.contestservice.contest.model.exception.EndAtInPastException;
import com.codeclash.contestservice.contest.model.exception.LiveAtInPastException;
import com.codeclash.contestservice.contest.model.question.ContestQuestions;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
class ContestScheduleRules {

  void requireCreatable(LiveAt liveAt, EndAt endAt) {
    Instant now = Instant.now();
    ContestScheduleRules.requireFutureLiveAt(liveAt, now);
    ContestScheduleRules.requireFutureEndAt(endAt, now);
  }

  void requireWritable(Contest previous, LiveAt liveAt, EndAt endAt, ContestQuestions questions) {
    if (previous == null) {
      this.requireCreatable(liveAt, endAt);
      return;
    }
    Instant now = Instant.now();
    boolean liveAtChanged = !previous.liveAt().equals(liveAt);
    boolean questionsChanged = !previous.questions().equals(questions);
    if ((liveAtChanged || questionsChanged) && ContestScheduleRules.isStarted(previous, now))
      throw ContestScheduleRules.alreadyLive(previous);
    if (liveAtChanged) ContestScheduleRules.requireFutureLiveAt(liveAt, now);
    if (!previous.endAt().equals(endAt)) ContestScheduleRules.requireFutureEndAt(endAt, now);
  }

  void requireDeletable(Contest contest) {
    if (ContestScheduleRules.isStarted(contest, Instant.now()))
      throw ContestScheduleRules.alreadyLive(contest);
  }

  boolean isStarted(Contest contest) {
    return ContestScheduleRules.isStarted(contest, Instant.now());
  }

  private static boolean isStarted(Contest contest, Instant now) {
    return contest.openedAt() != null || !contest.liveAt().value().isAfter(now);
  }

  private static void requireFutureLiveAt(LiveAt liveAt, Instant now) {
    if (!liveAt.value().isAfter(now)) throw new LiveAtInPastException(liveAt.value());
  }

  private static void requireFutureEndAt(EndAt endAt, Instant now) {
    if (!endAt.value().isAfter(now)) throw new EndAtInPastException(endAt.value());
  }

  private static ContestAlreadyLiveException alreadyLive(Contest contest) {
    return new ContestAlreadyLiveException(contest.id().value(), contest.liveAt().value());
  }
}
