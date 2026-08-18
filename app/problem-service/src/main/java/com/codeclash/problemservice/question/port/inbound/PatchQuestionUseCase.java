package com.codeclash.problemservice.question.port.inbound;

import com.codeclash.problemservice.question.model.Question;
import com.codeclash.problemservice.question.model.request.PatchQuestionRequest;

public interface PatchQuestionUseCase {

  Question patch(
      PatchQuestionRequest request);
}
