package com.codeclash.problemservice.question.service;

import com.codeclash.problemservice.question.model.Question;
import com.codeclash.problemservice.question.model.request.CreateQuestionRequest;
import com.codeclash.problemservice.question.port.inbound.CreateQuestionUseCase;
import com.codeclash.problemservice.question.port.outbound.QuestionEventPublisher;
import com.codeclash.problemservice.question.port.outbound.QuestionPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class CreateQuestionService implements CreateQuestionUseCase {

  private final QuestionPersistence questionPersistence;
  private final QuestionEventPublisher questionEventPublisher;

  @Override
  @Transactional
  public Question create(CreateQuestionRequest request) {
    Question question = this.questionPersistence.create(request);
    this.questionEventPublisher.written(question);
    CreateQuestionService.log
        .atInfo()
        .setMessage("question created")
        .addKeyValue("question.id", question.id().value())
        .log();
    return question;
  }
}
