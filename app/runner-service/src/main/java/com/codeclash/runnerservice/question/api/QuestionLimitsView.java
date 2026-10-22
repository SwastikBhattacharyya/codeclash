package com.codeclash.runnerservice.question.api;

import java.util.UUID;

public record QuestionLimitsView(UUID id, int maxRunTimeMs, int maxRamLimitMb) {}
