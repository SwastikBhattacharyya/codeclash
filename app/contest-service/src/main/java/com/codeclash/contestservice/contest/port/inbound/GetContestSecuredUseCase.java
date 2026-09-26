package com.codeclash.contestservice.contest.port.inbound;

import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.result.ContestDetail;

public interface GetContestSecuredUseCase {

  ContestDetail findById(Id id);
}
