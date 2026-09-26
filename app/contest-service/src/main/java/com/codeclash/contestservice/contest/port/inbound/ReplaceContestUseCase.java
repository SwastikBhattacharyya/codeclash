package com.codeclash.contestservice.contest.port.inbound;

import com.codeclash.contestservice.contest.model.request.ReplaceContestRequest;
import com.codeclash.contestservice.contest.model.result.ReplaceContestResult;

public interface ReplaceContestUseCase {

  ReplaceContestResult replace(ReplaceContestRequest request);
}
