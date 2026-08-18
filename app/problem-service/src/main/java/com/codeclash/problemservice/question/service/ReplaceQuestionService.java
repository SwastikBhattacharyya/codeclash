package com.codeclash.problemservice.question.service;

import com.codeclash.problemservice.question.model.request.ReplaceQuestionRequest;
import com.codeclash.problemservice.question.model.result.ReplaceQuestionResult;
import com.codeclash.problemservice.question.port.inbound.ReplaceQuestionUseCase;
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
public class ReplaceQuestionService implements ReplaceQuestionUseCase {

  private final QuestionPersistence questionPersistence;
  private final QuestionEventPublisher questionEventPublisher;
  private final QuestionContestRules questionContestRules;

  @Override
  @Transactional
  public ReplaceQuestionResult replace(ReplaceQuestionRequest request) {
    if (!request.visible().value()) this.questionContestRules.requireHideable(request.id());
    ReplaceQuestionResult result = this.questionPersistence.replace(request);
    this.questionEventPublisher.written(result.question());
    ReplaceQuestionService.log
        .atInfo()
        .setMessage("question replaced")
        .addKeyValue("question.id", result.question().id().value())
        .addKeyValue("created", result.created())
        .log();
    return result;
  }
}
