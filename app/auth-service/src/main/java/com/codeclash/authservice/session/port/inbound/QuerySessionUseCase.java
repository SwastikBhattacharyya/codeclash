package com.codeclash.authservice.session.port.inbound;

import com.codeclash.authservice.session.model.request.QuerySessionRequest;
import com.codeclash.authservice.session.model.result.AuthorizedSession;
import com.codeclash.common.page.PagedEntity;

public interface QuerySessionUseCase {

  PagedEntity<AuthorizedSession> query(QuerySessionRequest request);
}
