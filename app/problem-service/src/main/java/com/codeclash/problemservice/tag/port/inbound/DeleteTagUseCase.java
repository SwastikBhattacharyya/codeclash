package com.codeclash.problemservice.tag.port.inbound;

import com.codeclash.problemservice.tag.model.Id;

public interface DeleteTagUseCase {

  void delete(Id id);
}
