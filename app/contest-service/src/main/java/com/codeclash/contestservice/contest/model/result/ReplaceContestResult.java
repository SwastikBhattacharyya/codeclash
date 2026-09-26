package com.codeclash.contestservice.contest.model.result;

import com.codeclash.contestservice.contest.model.Contest;

public record ReplaceContestResult(Contest contest, boolean created) {}
