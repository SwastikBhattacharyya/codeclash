package com.codeclash.contestservice.contest.port.outbound;

import com.codeclash.contestservice.contest.model.Contest;

public interface ContestEventPublisher {

  void written(Contest contest);

  void deleted(Contest contest);
}
