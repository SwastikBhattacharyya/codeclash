package com.codeclash.problemservice.tag.service;

import com.codeclash.problemservice.tag.model.Tag;
import com.codeclash.problemservice.tag.model.request.CreateTagRequest;
import com.codeclash.problemservice.tag.port.inbound.CreateTagUseCase;
import com.codeclash.problemservice.tag.port.outbound.TagPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class CreateTagService implements CreateTagUseCase {

  private final TagPersistence tagPersistence;

  @Override
  public Tag create(CreateTagRequest request) {
    Tag tag = this.tagPersistence.create(request);
    CreateTagService.log
        .atInfo()
        .setMessage("tag created")
        .addKeyValue("tag.id", tag.id().value())
        .log();
    return tag;
  }
}
