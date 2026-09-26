package com.codeclash.contestservice.contest.port.inbound;

import com.codeclash.contestservice.contest.model.Id;

public interface DeleteContestSecuredUseCase {

  void delete(Id id);
}
