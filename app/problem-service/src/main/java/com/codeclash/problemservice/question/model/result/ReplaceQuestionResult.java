package com.codeclash.problemservice.question.model.result;

import com.codeclash.problemservice.question.model.Question;

public record ReplaceQuestionResult(Question question, boolean created) {}
