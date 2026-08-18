package com.codeclash.problemservice.question.port.inbound;

import com.codeclash.problemservice.question.model.Id;
import java.util.List;

public interface OpenQuestionsUseCase {

  void open(List<Id> ids);
}
