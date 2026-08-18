package com.codeclash.problemservice.question.port.outbound;

import com.codeclash.problemservice.question.model.Id;
import java.util.List;
import java.util.UUID;

public interface RunningContestLookup {

  List<UUID> findRunningIds(Id questionId);
}
