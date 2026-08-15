package com.codeclash.problemservice.tag.service;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.problemservice.tag.model.Tag;
import com.codeclash.problemservice.tag.model.request.QueryTagRequest;
import com.codeclash.problemservice.tag.port.inbound.QueryTagUseCase;
import com.codeclash.problemservice.tag.port.outbound.TagPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Observed
@RequiredArgsConstructor
public class QueryTagService implements QueryTagUseCase {

  private final TagPersistence tagPersistence;

  @Override
  @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
  public PagedEntity<Tag> query(QueryTagRequest request) {
    return this.tagPersistence.query(request);
  }
}
