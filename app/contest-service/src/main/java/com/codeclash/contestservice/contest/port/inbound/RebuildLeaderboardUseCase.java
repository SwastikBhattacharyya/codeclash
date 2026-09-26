package com.codeclash.contestservice.contest.port.inbound;

import com.codeclash.contestservice.contest.model.Id;
import java.util.List;

public interface RebuildLeaderboardUseCase {

  void lock(List<Id> ids);

  void rebuild(List<Id> ids);
}
