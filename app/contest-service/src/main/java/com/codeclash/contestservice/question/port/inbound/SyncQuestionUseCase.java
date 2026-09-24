package com.codeclash.contestservice.question.port.inbound;

import com.codeclash.contestservice.question.model.Id;
import com.codeclash.contestservice.question.model.Question;

public interface SyncQuestionUseCase {

  void write(Question question);

  void delete(Id id);
}
