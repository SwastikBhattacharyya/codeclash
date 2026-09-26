package com.codeclash.contestservice.contest.port.inbound;

import com.codeclash.contestservice.contest.model.Contest;
import java.time.Instant;
import java.util.List;

public interface OpenDueContestsUseCase {

  List<Contest> openDue(Instant cutoff, int limit);
}
