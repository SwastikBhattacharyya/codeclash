package com.codeclash.problemservice.question.service;

import com.codeclash.common.patch.PatchObject;
import com.codeclash.contracts.auth.session.CurrentSession;
import com.codeclash.contracts.auth.session.exception.ForbiddenException;
import com.codeclash.contracts.problem.ProblemPermissions;
import com.codeclash.problemservice.question.model.Id;
import com.codeclash.problemservice.question.model.Question;
import com.codeclash.problemservice.question.model.Visible;
import com.codeclash.problemservice.question.model.exception.QuestionNotFoundException;
import com.codeclash.problemservice.question.model.request.QueryQuestionRequest;
import com.codeclash.problemservice.question.port.inbound.GetQuestionUseCase;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class QuestionVisibilityGuard {

  private final CurrentSession currentSession;
  private final GetQuestionUseCase getQuestionUseCase;

  QueryQuestionRequest visibleFilter(QueryQuestionRequest request) {
    if (this.has(ProblemPermissions.QUESTION_READ_IS_PRIVATE)) return request;
    if (request.visible() == null) return request.toBuilder().visible(true).build();
    if (!request.visible())
      throw QuestionVisibilityGuard.forbidden(ProblemPermissions.QUESTION_READ_IS_PRIVATE);
    return request;
  }

  void requireReadable(Question question) {
    if (!question.visible().value() && !this.has(ProblemPermissions.QUESTION_READ_IS_PRIVATE))
      throw QuestionVisibilityGuard.forbidden(ProblemPermissions.QUESTION_READ_IS_PRIVATE);
  }

  void requireCreatable(Visible visible) {
    if (!visible.value()) this.requireWritePrivate();
  }

  void requireReplaceable(Id id, Visible visible) {
    if (this.has(ProblemPermissions.QUESTION_WRITE_IS_PRIVATE)) return;
    if (!visible.value()) this.requireWritePrivate();
    this.findStored(id).ifPresent(this::requireStoredWritable);
  }

  void requirePatchable(Id id, PatchObject<Visible> visible) {
    if (this.has(ProblemPermissions.QUESTION_WRITE_IS_PRIVATE)) return;
    if (visible.isPresent() && !visible.orElse(null).value()) this.requireWritePrivate();
    this.requireStoredWritable(this.getQuestionUseCase.findById(id));
  }

  void requireDeletable(Id id) {
    if (this.has(ProblemPermissions.QUESTION_WRITE_IS_PRIVATE)) return;
    this.requireStoredWritable(this.getQuestionUseCase.findById(id));
  }

  private void requireStoredWritable(Question question) {
    if (!question.visible().value()) this.requireWritePrivate();
  }

  private void requireWritePrivate() {
    if (!this.has(ProblemPermissions.QUESTION_WRITE_IS_PRIVATE))
      throw QuestionVisibilityGuard.forbidden(ProblemPermissions.QUESTION_WRITE_IS_PRIVATE);
  }

  private Optional<Question> findStored(Id id) {
    try {
      return Optional.of(this.getQuestionUseCase.findById(id));
    } catch (QuestionNotFoundException exception) {
      return Optional.empty();
    }
  }

  private boolean has(String permission) {
    return this.currentSession.hasPermission(permission);
  }

  private static ForbiddenException forbidden(String permission) {
    return new ForbiddenException(List.of(permission));
  }
}
