package com.codeclash.runnerservice.submission.port.inbound;

import com.codeclash.runnerservice.submission.model.JobCount;
import java.util.List;

public interface CountJobsUseCase {

  List<JobCount> count();
}
