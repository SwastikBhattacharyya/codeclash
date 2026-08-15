package com.codeclash.problemservice.tag.port.inbound;

import com.codeclash.problemservice.tag.model.Id;
import com.codeclash.problemservice.tag.model.Tag;

public interface GetTagUseCase {

  Tag findById(Id id);
}
