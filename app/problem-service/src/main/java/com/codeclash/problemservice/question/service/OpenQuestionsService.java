package com.codeclash.problemservice.question.service;

import com.codeclash.common.patch.PatchObject;
import com.codeclash.problemservice.question.model.Id;
import com.codeclash.problemservice.question.model.Question;
import com.codeclash.problemservice.question.model.Visible;
import com.codeclash.problemservice.question.model.exception.QuestionNotFoundException;
import com.codeclash.problemservice.question.model.request.PatchQuestionRequest;
import com.codeclash.problemservice.question.port.inbound.OpenQuestionsUseCase;
import com.codeclash.problemservice.question.port.inbound.PatchQuestionUseCase;
import com.codeclash.problemservice.question.port.outbound.QuestionPersistence;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class OpenQuestionsService implements OpenQuestionsUseCase {

  private final QuestionPersistence questionPersistence;
  private final PatchQuestionUseCase patchQuestionUseCase;

  private static PatchQuestionRequest visibleRequest(Id id) {
    return new PatchQuestionRequest(
        id,
        PatchObject.from(JsonNullable.undefined()),
        PatchObject.from(JsonNullable.undefined()),
        PatchObject.from(JsonNullable.undefined()),
        PatchObject.of(new Visible(true)),
        PatchObject.from(JsonNullable.undefined()),
        PatchObject.from(JsonNullable.undefined()),
        PatchObject.from(JsonNullable.undefined()),
        PatchObject.from(JsonNullable.undefined()));
  }

  @Override
  public void open(List<Id> ids) {
    long opened = ids.stream().filter(this::open).count();
    if (opened == 0) return;
    OpenQuestionsService.log
        .atInfo()
        .setMessage("questions opened")
        .addKeyValue("count", opened)
        .log();
  }

  private boolean open(Id id) {
    Optional<Question> question = this.questionPersistence.findById(id);
    if (question.isEmpty() || question.get().visible().value()) return false;
    try {
      this.patchQuestionUseCase.patch(OpenQuestionsService.visibleRequest(id));
      return true;
    } catch (QuestionNotFoundException exception) {
      return false;
    }
  }
}
