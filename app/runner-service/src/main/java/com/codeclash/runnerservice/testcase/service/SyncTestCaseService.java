package com.codeclash.runnerservice.testcase.service;

import com.codeclash.runnerservice.testcase.model.Id;
import com.codeclash.runnerservice.testcase.model.TestCase;
import com.codeclash.runnerservice.testcase.port.inbound.SyncTestCaseUseCase;
import com.codeclash.runnerservice.testcase.port.outbound.TestCasePersistence;
import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Observed
@RequiredArgsConstructor
public class SyncTestCaseService implements SyncTestCaseUseCase {

  private final TestCasePersistence testCasePersistence;

  @Override
  public void write(TestCase testCase) {
    this.testCasePersistence.upsert(testCase);
    SyncTestCaseService.log
        .atDebug()
        .setMessage("test case synced")
        .addKeyValue("test_case.id", testCase.id().value())
        .log();
  }

  @Override
  public void delete(Id id) {
    this.testCasePersistence.deleteById(id);
    SyncTestCaseService.log
        .atDebug()
        .setMessage("test case removed")
        .addKeyValue("test_case.id", id.value())
        .log();
  }
}
