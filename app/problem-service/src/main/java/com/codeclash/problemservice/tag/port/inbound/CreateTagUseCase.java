package com.codeclash.problemservice.tag.port.inbound;

import com.codeclash.problemservice.tag.model.Tag;
import com.codeclash.problemservice.tag.model.request.CreateTagRequest;

public interface CreateTagUseCase {

  Tag create(CreateTagRequest request);
}
