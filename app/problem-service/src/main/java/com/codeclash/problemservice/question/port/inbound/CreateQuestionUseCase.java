package com.codeclash.problemservice.question.port.inbound;

import com.codeclash.problemservice.question.model.Question;
import com.codeclash.problemservice.question.model.request.CreateQuestionRequest;

public interface CreateQuestionUseCase {

  Question create(
      CreateQuestionRequest request);
}
