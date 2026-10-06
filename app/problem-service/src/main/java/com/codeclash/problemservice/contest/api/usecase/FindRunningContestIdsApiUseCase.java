package com.codeclash.problemservice.contest.api.usecase;

import java.util.List;
import java.util.UUID;

public interface FindRunningContestIdsApiUseCase {

  List<UUID> findRunningIds(UUID questionId);
}
