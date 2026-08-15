package com.codeclash.problemservice.tag.service;

import com.codeclash.problemservice.tag.model.Id;
import com.codeclash.problemservice.tag.model.exception.TagNotFoundException;
import com.codeclash.problemservice.tag.port.inbound.DeleteTagUseCase;
import com.codeclash.problemservice.tag.port.outbound.TagPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class DeleteTagService implements DeleteTagUseCase {

  private final TagPersistence tagPersistence;

  @Override
  public void delete(Id id) {
    if (!this.tagPersistence.deleteById(id)) throw new TagNotFoundException(id.value());
    DeleteTagService.log
        .atInfo()
        .setMessage("tag deleted")
        .addKeyValue("tag.id", id.value())
        .log();
  }
}
