package com.codeclash.problemservice.contest.port.outbound;

import com.codeclash.problemservice.contest.model.Contest;

public interface ContestOpenedPublisher {

  void opened(Contest contest);
}
