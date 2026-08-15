package com.codeclash.problemservice.tag.port.inbound;

import com.codeclash.problemservice.tag.model.Tag;
import com.codeclash.problemservice.tag.model.request.PatchTagRequest;

public interface PatchTagUseCase {

  Tag patch(PatchTagRequest request);
}
