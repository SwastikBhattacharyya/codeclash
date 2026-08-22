package com.codeclash.problemservice.testcase.service;

import com.codeclash.problemservice.testcase.api.usecase.FindPublicTestCaseIdsApiUseCase;
import com.codeclash.problemservice.testcase.model.Id;
import com.codeclash.problemservice.testcase.port.outbound.TestCasePersistence;
import io.micrometer.observation.annotation.Observed;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class TestCaseApiService implements FindPublicTestCaseIdsApiUseCase {

  private final TestCasePersistence testCasePersistence;

  @Override
  public Set<UUID> findPublicIds(Collection<UUID> ids) {
    if (ids.isEmpty()) return Set.of();
    return this.testCasePersistence.findPublicIds(ids.stream().map(Id::unsafe).toList()).stream()
        .map(Id::value)
        .collect(Collectors.toUnmodifiableSet());
  }
}
