package com.codeclash.contestservice.user.adapter.outbound.contest;

import com.codeclash.contestservice.contest.api.usecase.ContestLeaderboardApiUseCase;
import com.codeclash.contestservice.user.model.Id;
import com.codeclash.contestservice.user.port.outbound.ContestLeaderboards;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserContestApiAdapter implements ContestLeaderboards {

  private final ContestLeaderboardApiUseCase contestLeaderboardApiUseCase;

  @Override
  public List<UUID> findRegisteredContestIds(Id id) {
    return this.contestLeaderboardApiUseCase.findRegisteredContestIds(id.value());
  }

  @Override
  public void lock(List<UUID> contestIds) {
    this.contestLeaderboardApiUseCase.lock(contestIds);
  }

  @Override
  public void rebuild(List<UUID> contestIds) {
    this.contestLeaderboardApiUseCase.rebuild(contestIds);
  }
}
