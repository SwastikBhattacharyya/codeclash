package com.codeclash.problemservice.question.service;

import com.codeclash.problemservice.question.model.Question;
import com.codeclash.problemservice.question.model.exception.QuestionNotFoundException;
import com.codeclash.problemservice.question.model.request.PatchQuestionRequest;
import com.codeclash.problemservice.question.port.inbound.PatchQuestionUseCase;
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
public class PatchQuestionService implements PatchQuestionUseCase {

  private final QuestionPersistence questionPersistence;
  private final QuestionEventPublisher questionEventPublisher;
  private final QuestionContestRules questionContestRules;

  @Override
  @Transactional
  public Question patch(PatchQuestionRequest request) {
    if (request.visible().isPresent() && !request.visible().orElse(null).value())
      this.questionContestRules.requireHideable(request.id());
    Question question =
        this.questionPersistence
            .patch(request)
            .orElseThrow(() -> new QuestionNotFoundException(request.id().value()));
    this.questionEventPublisher.written(question);
    PatchQuestionService.log
        .atInfo()
        .setMessage("question patched")
        .addKeyValue("question.id", question.id().value())
        .log();
    return question;
  }
}
