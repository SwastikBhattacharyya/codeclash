package com.codeclash.contestservice.contest.port.inbound;

import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.model.request.CreateContestRequest;

public interface CreateContestUseCase {

  Contest create(CreateContestRequest request);
}
