package com.codeclash.contestservice.submission.adapter.outbound.contest;

import com.codeclash.contestservice.contest.api.usecase.ContestLeaderboardApiUseCase;
import com.codeclash.contestservice.submission.model.Submission;
import com.codeclash.contestservice.submission.port.outbound.LeaderboardRebuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubmissionContestApiAdapter implements LeaderboardRebuilder {

  private final ContestLeaderboardApiUseCase contestLeaderboardApiUseCase;

  @Override
  public void rebuildFor(Submission submission) {
    this.contestLeaderboardApiUseCase.rebuildForSubmission(
        submission.questionId(), submission.userId(), submission.createdAt().value());
  }
}
