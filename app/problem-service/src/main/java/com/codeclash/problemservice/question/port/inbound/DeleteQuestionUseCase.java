package com.codeclash.problemservice.question.port.inbound;

import com.codeclash.problemservice.question.model.Id;

public interface DeleteQuestionUseCase {

  void delete(Id id);
}
