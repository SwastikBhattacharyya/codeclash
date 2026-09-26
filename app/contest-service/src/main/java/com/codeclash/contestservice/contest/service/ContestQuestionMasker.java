package com.codeclash.contestservice.contest.service;

import com.codeclash.contracts.auth.session.CurrentSession;
import com.codeclash.contracts.contest.ContestPermissions;
import com.codeclash.contestservice.contest.model.Contest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ContestQuestionMasker {

  private final CurrentSession currentSession;
  private final ContestScheduleRules contestScheduleRules;

  Contest mask(Contest contest) {
    if (this.contestScheduleRules.isStarted(contest)) return contest;
    if (this.currentSession.hasPermission(ContestPermissions.CONTEST_READ_QUESTIONS))
      return contest;
    return contest.withQuestions(null);
  }
}
