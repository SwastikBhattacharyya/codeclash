package com.codeclash.problemservice.question.port.outbound;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.problemservice.question.model.Id;
import com.codeclash.problemservice.question.model.Question;
import com.codeclash.problemservice.question.model.request.CreateQuestionRequest;
import com.codeclash.problemservice.question.model.request.PatchQuestionRequest;
import com.codeclash.problemservice.question.model.request.QueryQuestionRequest;
import com.codeclash.problemservice.question.model.request.ReplaceQuestionRequest;
import com.codeclash.problemservice.question.model.result.ReplaceQuestionResult;
import java.util.Optional;

public interface QuestionPersistence {

  PagedEntity<Question> query(QueryQuestionRequest request);

  Optional<Question> findById(Id id);

  Question create(
      CreateQuestionRequest request);

  ReplaceQuestionResult replace(
      ReplaceQuestionRequest request);

  Optional<Question> patch(
      PatchQuestionRequest request);

  Optional<Question> deleteById(Id id);
}
