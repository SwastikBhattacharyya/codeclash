package com.codeclash.contestservice.question.service;

import com.codeclash.contestservice.question.model.Id;
import com.codeclash.contestservice.question.model.Question;
import com.codeclash.contestservice.question.port.inbound.SyncQuestionUseCase;
import com.codeclash.contestservice.question.port.outbound.QuestionPersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class SyncQuestionService implements SyncQuestionUseCase {

  private final QuestionPersistence questionPersistence;

  @Override
  public void write(Question question) {
    this.questionPersistence.upsert(question);
    SyncQuestionService.log
        .atDebug()
        .setMessage("question synced")
        .addKeyValue("question.id", question.id().value())
        .log();
  }

  @Override
  public void delete(Id id) {
    this.questionPersistence.deleteById(id);
    SyncQuestionService.log
        .atDebug()
        .setMessage("question removed")
        .addKeyValue("question.id", id.value())
        .log();
  }
}
