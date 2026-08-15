package com.codeclash.problemservice.tag.port.inbound;

import com.codeclash.problemservice.tag.model.request.ReplaceTagRequest;
import com.codeclash.problemservice.tag.model.result.ReplaceTagResult;

public interface ReplaceTagUseCase {

  ReplaceTagResult replace(ReplaceTagRequest request);
}
