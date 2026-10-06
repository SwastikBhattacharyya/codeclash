package com.codeclash.problemservice.contest.port.outbound;

import com.codeclash.problemservice.contest.model.Contest;
import com.codeclash.problemservice.contest.model.Id;
import com.codeclash.problemservice.contest.model.result.SyncContestResult;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContestPersistence {

  Optional<SyncContestResult> upsert(Contest contest);

  void replaceQuestions(Id id, List<UUID> questionIds);

  void deleteById(Id id);

  List<UUID> findRunningIds(UUID questionId, Instant now);
}
