package com.codeclash.contestservice.contest.port.inbound;

import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.model.request.PatchContestRequest;

public interface PatchContestSecuredUseCase {

  Contest patch(PatchContestRequest request);
}
