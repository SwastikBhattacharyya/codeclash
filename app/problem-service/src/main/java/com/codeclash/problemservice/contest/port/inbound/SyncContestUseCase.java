package com.codeclash.problemservice.contest.port.inbound;

import com.codeclash.problemservice.contest.model.Contest;
import com.codeclash.problemservice.contest.model.Id;

public interface SyncContestUseCase {

  void write(Contest contest);

  void delete(Id id);
}
