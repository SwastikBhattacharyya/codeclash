package com.codeclash.problemservice.question.service;

import com.codeclash.problemservice.question.model.Id;
import com.codeclash.problemservice.question.model.Question;
import com.codeclash.problemservice.question.model.exception.QuestionNotFoundException;
import com.codeclash.problemservice.question.port.inbound.DeleteQuestionUseCase;
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
public class DeleteQuestionService implements DeleteQuestionUseCase {

  private final QuestionPersistence questionPersistence;
  private final QuestionEventPublisher questionEventPublisher;
  private final QuestionContestRules questionContestRules;

  @Override
  @Transactional
  public void delete(Id id) {
    this.questionContestRules.requireDeletable(id);
    Question question =
        this.questionPersistence
            .deleteById(id)
            .orElseThrow(() -> new QuestionNotFoundException(id.value()));
    this.questionEventPublisher.deleted(question);
    DeleteQuestionService.log
        .atInfo()
        .setMessage("question deleted")
        .addKeyValue("question.id", question.id().value())
        .log();
  }
}
