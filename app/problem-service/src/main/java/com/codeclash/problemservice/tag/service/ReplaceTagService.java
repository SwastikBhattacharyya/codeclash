package com.codeclash.problemservice.tag.service;

import com.codeclash.problemservice.tag.model.request.ReplaceTagRequest;
import com.codeclash.problemservice.tag.model.result.ReplaceTagResult;
import com.codeclash.problemservice.tag.port.inbound.ReplaceTagUseCase;
import com.codeclash.problemservice.tag.port.outbound.TagPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class ReplaceTagService implements ReplaceTagUseCase {

  private final TagPersistence tagPersistence;

  @Override
  public ReplaceTagResult replace(ReplaceTagRequest request) {
    ReplaceTagResult result = this.tagPersistence.replace(request);
    ReplaceTagService.log
        .atInfo()
        .setMessage("tag replaced")
        .addKeyValue("tag.id", result.tag().id().value())
        .addKeyValue("created", result.created())
        .log();
    return result;
  }
}
