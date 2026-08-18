package com.codeclash.problemservice.question.port.inbound;

import com.codeclash.problemservice.question.model.request.ReplaceQuestionRequest;
import com.codeclash.problemservice.question.model.result.ReplaceQuestionResult;

public interface ReplaceQuestionSecuredUseCase {

  ReplaceQuestionResult replace(
      ReplaceQuestionRequest request);
}
