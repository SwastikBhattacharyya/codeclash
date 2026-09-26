package com.codeclash.contestservice.contest.port.outbound;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.question.ContestQuestions;
import com.codeclash.contestservice.contest.model.request.CreateContestRequest;
import com.codeclash.contestservice.contest.model.request.PatchContestRequest;
import com.codeclash.contestservice.contest.model.request.QueryContestRequest;
import com.codeclash.contestservice.contest.model.request.ReplaceContestRequest;
import com.codeclash.contestservice.contest.model.result.ContestWriteResult;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ContestPersistence {

  PagedEntity<Contest> query(QueryContestRequest request);

  Optional<Contest> findById(Id id);

  Optional<Contest> lockById(Id id);

  Contest create(CreateContestRequest request);

  ContestWriteResult replace(ReplaceContestRequest request);

  Optional<ContestWriteResult> patch(PatchContestRequest request);

  void replaceQuestions(Id id, ContestQuestions questions);

  List<Contest> openDue(Instant cutoff, int limit);

  Optional<Contest> deleteById(Id id);
}
