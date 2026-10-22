package com.codeclash.runnerservice.question.port.outbound;

import com.codeclash.runnerservice.question.model.Id;
import com.codeclash.runnerservice.question.model.Question;
import java.util.Optional;

public interface QuestionPersistence {

  void upsert(Question question);

  void deleteById(Id id);

  Optional<Question> findById(Id id);
}
