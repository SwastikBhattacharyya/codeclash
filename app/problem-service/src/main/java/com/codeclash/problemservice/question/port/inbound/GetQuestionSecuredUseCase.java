package com.codeclash.problemservice.question.port.inbound;

import com.codeclash.problemservice.question.model.Id;
import com.codeclash.problemservice.question.model.Question;

public interface GetQuestionSecuredUseCase {

  Question findById(Id id);
}
