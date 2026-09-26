package com.codeclash.contestservice.contest.model.request;

import com.codeclash.contestservice.contest.model.Description;
import com.codeclash.contestservice.contest.model.EndAt;
import com.codeclash.contestservice.contest.model.LiveAt;
import com.codeclash.contestservice.contest.model.Name;
import com.codeclash.contestservice.contest.model.question.ContestQuestions;

public record CreateContestRequest(
    Name name, Description description, LiveAt liveAt, EndAt endAt, ContestQuestions questions) {}
