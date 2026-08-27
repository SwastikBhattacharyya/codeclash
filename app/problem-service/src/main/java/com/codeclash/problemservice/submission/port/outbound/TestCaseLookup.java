package com.codeclash.problemservice.submission.port.outbound;

import com.codeclash.problemservice.submission.model.FailedTestCaseId;
import java.util.Collection;
import java.util.Set;

public interface TestCaseLookup {

  Set<FailedTestCaseId> findPublic(Collection<FailedTestCaseId> ids);
}
