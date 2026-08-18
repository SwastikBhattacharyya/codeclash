package com.codeclash.problemservice.question.api.usecase;

import com.codeclash.problemservice.question.api.QuestionView;
import java.util.UUID;

public interface FindQuestionByIdApiUseCase {

  QuestionView findById(UUID id);
}
