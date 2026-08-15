package com.codeclash.problemservice.tag.port.outbound;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.problemservice.tag.model.Id;
import com.codeclash.problemservice.tag.model.Tag;
import com.codeclash.problemservice.tag.model.request.CreateTagRequest;
import com.codeclash.problemservice.tag.model.request.PatchTagRequest;
import com.codeclash.problemservice.tag.model.request.QueryTagRequest;
import com.codeclash.problemservice.tag.model.request.ReplaceTagRequest;
import com.codeclash.problemservice.tag.model.result.ReplaceTagResult;
import java.util.Optional;

public interface TagPersistence {

  PagedEntity<Tag> query(QueryTagRequest request);

  Optional<Tag> findById(Id id);

  Tag create(CreateTagRequest request);

  ReplaceTagResult replace(ReplaceTagRequest request);

  Optional<Tag> patch(PatchTagRequest request);

  boolean deleteById(Id id);
}
