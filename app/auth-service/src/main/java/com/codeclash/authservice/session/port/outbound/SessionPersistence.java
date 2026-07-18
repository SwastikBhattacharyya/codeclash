package com.codeclash.authservice.session.port.outbound;

import com.codeclash.authservice.session.model.Id;
import com.codeclash.authservice.session.model.Session;
import com.codeclash.authservice.session.model.UserId;
import com.codeclash.authservice.session.model.request.PatchSessionRequest;
import com.codeclash.authservice.session.model.request.QuerySessionRequest;
import com.codeclash.common.page.PagedEntity;
import java.util.Optional;

public interface SessionPersistence {

  PagedEntity<Session> query(QuerySessionRequest request);

  Optional<Session> findById(Id id);

  Session create(UserId userId);

  Optional<Session> patch(PatchSessionRequest request);

  boolean deleteById(Id id);
}
