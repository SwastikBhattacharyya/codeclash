package com.codeclash.problemservice.question.port.outbound;

import com.codeclash.problemservice.question.model.Question;

public interface QuestionEventPublisher {

  void written(Question question);

  void deleted(Question question);
}
