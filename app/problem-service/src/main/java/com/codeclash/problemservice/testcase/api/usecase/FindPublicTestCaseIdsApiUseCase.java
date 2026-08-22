package com.codeclash.problemservice.testcase.api.usecase;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface FindPublicTestCaseIdsApiUseCase {

  Set<UUID> findPublicIds(Collection<UUID> ids);
}
