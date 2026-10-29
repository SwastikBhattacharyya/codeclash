package com.codeclash.runnerservice.submission.adapter.outbound.testcase;

import com.codeclash.runnerservice.submission.model.ExpectedOutput;
import com.codeclash.runnerservice.submission.model.JudgeTestCase;
import com.codeclash.runnerservice.submission.model.QuestionId;
import com.codeclash.runnerservice.submission.model.TestCaseId;
import com.codeclash.runnerservice.submission.port.outbound.TestCaseLookup;
import com.codeclash.runnerservice.testcase.api.usecase.FindTestCasesByQuestionApiUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestCaseApiAdapter implements TestCaseLookup {

  private final FindTestCasesByQuestionApiUseCase findTestCasesByQuestionApiUseCase;

  @Override
  public List<JudgeTestCase> findByQuestionId(QuestionId questionId) {
    return this.findTestCasesByQuestionApiUseCase.findByQuestionId(questionId.value()).stream()
        .map(
            view ->
                new JudgeTestCase(
                    TestCaseId.unsafe(view.id()),
                    view.input(),
                    new ExpectedOutput(view.output()),
                    view.isPublic()))
        .toList();
  }
}
