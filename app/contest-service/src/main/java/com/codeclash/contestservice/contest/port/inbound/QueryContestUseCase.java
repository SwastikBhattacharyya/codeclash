package com.codeclash.contestservice.contest.port.inbound;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.contestservice.contest.model.Contest;
import com.codeclash.contestservice.contest.model.request.QueryContestRequest;

public interface QueryContestUseCase {

  PagedEntity<Contest> query(QueryContestRequest request);
}
