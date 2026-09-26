package com.codeclash.contestservice.contest.port.inbound;

import com.codeclash.contestservice.contest.model.Id;

public interface RegisterContestSecuredUseCase {

  void register(Id id);
}
