package com.codeclash.problemservice.question.port.inbound;

import com.codeclash.problemservice.question.model.Question;
import com.codeclash.problemservice.question.model.request.CreateQuestionRequest;

public interface CreateQuestionSecuredUseCase {

  Question create(
      CreateQuestionRequest request);
}
