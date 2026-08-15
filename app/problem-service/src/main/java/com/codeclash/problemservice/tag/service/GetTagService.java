package com.codeclash.problemservice.tag.service;

import com.codeclash.problemservice.tag.model.Id;
import com.codeclash.problemservice.tag.model.Tag;
import com.codeclash.problemservice.tag.model.exception.TagNotFoundException;
import com.codeclash.problemservice.tag.port.inbound.GetTagUseCase;
import com.codeclash.problemservice.tag.port.outbound.TagPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class GetTagService implements GetTagUseCase {

  private final TagPersistence tagPersistence;

  @Override
  public Tag findById(Id id) {
    return this.tagPersistence.findById(id).orElseThrow(() -> new TagNotFoundException(id.value()));
  }
}
