package com.codeclash.problemservice.tag.port.inbound;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.problemservice.tag.model.Tag;
import com.codeclash.problemservice.tag.model.request.QueryTagRequest;

public interface QueryTagUseCase {

  PagedEntity<Tag> query(QueryTagRequest request);
}
