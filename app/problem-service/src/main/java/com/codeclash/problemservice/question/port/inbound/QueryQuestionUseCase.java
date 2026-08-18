package com.codeclash.problemservice.question.port.inbound;

import com.codeclash.common.page.PagedEntity;
import com.codeclash.problemservice.question.model.Question;
import com.codeclash.problemservice.question.model.request.QueryQuestionRequest;

public interface QueryQuestionUseCase {

  PagedEntity<Question> query(QueryQuestionRequest request);
}
