package com.codeclash.contestservice.contest.model.request;

import com.codeclash.common.patch.PatchObject;
import com.codeclash.contestservice.contest.model.Description;
import com.codeclash.contestservice.contest.model.EndAt;
import com.codeclash.contestservice.contest.model.Id;
import com.codeclash.contestservice.contest.model.LiveAt;
import com.codeclash.contestservice.contest.model.Name;
import com.codeclash.contestservice.contest.model.question.ContestQuestions;

public record PatchContestRequest(
    Id id,
    PatchObject<Name> name,
    PatchObject<Description> description,
    PatchObject<LiveAt> liveAt,
    PatchObject<EndAt> endAt,
    PatchObject<ContestQuestions> questions) {}
