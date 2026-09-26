package com.codeclash.contestservice.contest.port.inbound;

import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.model.Id;

public interface GetContestUseCase {

  Contest findById(Id id);
}
