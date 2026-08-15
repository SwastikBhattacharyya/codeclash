package com.codeclash.problemservice.tag.port.inbound;

import com.codeclash.problemservice.tag.model.Id;

public interface DeleteTagSecuredUseCase {

  void delete(Id id);
}
