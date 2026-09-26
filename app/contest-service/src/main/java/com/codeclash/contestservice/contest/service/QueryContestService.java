package com.codeclash.contestservice.contest.service;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.model.request.QueryContestRequest;
import com.codeclash.contestservice.contest.port.inbound.QueryContestUseCase;
import com.codeclash.contestservice.contest.port.outbound.ContestPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Observed
@RequiredArgsConstructor
public class QueryContestService implements QueryContestUseCase {

  private final ContestPersistence contestPersistence;

  @Override
  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public PagedEntity<Contest> query(QueryContestRequest request) {
    return this.contestPersistence.query(request);
  }
}
