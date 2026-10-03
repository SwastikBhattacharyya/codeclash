package com.codeclash.contestservice.submission.port.outbound;

import com.codeclash.contestservice.submission.model.Submission;

public interface LeaderboardRebuilder {

  void rebuildFor(Submission submission);
}
