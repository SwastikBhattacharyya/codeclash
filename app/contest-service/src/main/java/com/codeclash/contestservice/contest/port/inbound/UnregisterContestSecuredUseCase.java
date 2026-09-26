package com.codeclash.contestservice.contest.port.inbound;

import com.codeclash.contestservice.contest.model.Id;

public interface UnregisterContestSecuredUseCase {

  void unregister(Id id);
}
