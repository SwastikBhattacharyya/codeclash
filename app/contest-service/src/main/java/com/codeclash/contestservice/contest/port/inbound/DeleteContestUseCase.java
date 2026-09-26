package com.codeclash.contestservice.contest.port.inbound;

import com.codeclash.contestservice.contest.model.Id;

public interface DeleteContestUseCase {

  void delete(Id id);
}
