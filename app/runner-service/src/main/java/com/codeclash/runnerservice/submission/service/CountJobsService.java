package com.codeclash.runnerservice.submission.service;

import com.codeclash.runnerservice.submission.model.JobCount;
import com.codeclash.runnerservice.submission.port.inbound.CountJobsUseCase;
import com.codeclash.runnerservice.submission.port.outbound.JobPersistence;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Observed
@RequiredArgsConstructor
public class CountJobsService implements CountJobsUseCase {

  private final JobPersistence jobPersistence;

  @Override
  public List<JobCount> count() {
    return this.jobPersistence.countByStatus();
  }
}
