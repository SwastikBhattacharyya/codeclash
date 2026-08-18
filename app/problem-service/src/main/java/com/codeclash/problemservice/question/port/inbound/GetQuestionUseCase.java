package com.codeclash.problemservice.question.port.inbound;

import com.codeclash.problemservice.question.model.Id;
import com.codeclash.problemservice.question.model.Question;

public interface GetQuestionUseCase {

  Question findById(Id id);
}
