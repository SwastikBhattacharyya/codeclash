package com.codeclash.contestservice.question.port.outbound;

import com.codeclash.contestservice.question.model.Id;
import com.codeclash.contestservice.question.model.Question;

public interface QuestionPersistence {

  void upsert(Question question);

  void deleteById(Id id);
}
