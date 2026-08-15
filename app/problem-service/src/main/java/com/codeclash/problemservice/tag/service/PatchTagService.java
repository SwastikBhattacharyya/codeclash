package com.codeclash.problemservice.tag.service;

import com.codeclash.problemservice.tag.model.Tag;
import com.codeclash.problemservice.tag.model.exception.TagNotFoundException;
import com.codeclash.problemservice.tag.model.request.PatchTagRequest;
import com.codeclash.problemservice.tag.port.inbound.PatchTagUseCase;
import com.codeclash.problemservice.tag.port.outbound.TagPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class PatchTagService implements PatchTagUseCase {

  private final TagPersistence tagPersistence;

  @Override
  public Tag patch(PatchTagRequest request) {
    Tag tag =
        this.tagPersistence
            .patch(request)
            .orElseThrow(() -> new TagNotFoundException(request.id().value()));
    PatchTagService.log
        .atInfo()
        .setMessage("tag patched")
        .addKeyValue("tag.id", tag.id().value())
        .log();
    return tag;
  }
}
