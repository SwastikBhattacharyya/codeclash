package com.codeclash.runnerservice.question.port.inbound;

import com.codeclash.runnerservice.question.model.Id;
import com.codeclash.runnerservice.question.model.Question;

public interface SyncQuestionUseCase {

  void write(Question question);

  void delete(Id id);
}
