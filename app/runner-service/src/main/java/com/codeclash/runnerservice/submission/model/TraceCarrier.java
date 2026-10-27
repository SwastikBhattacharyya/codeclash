package com.codeclash.runnerservice.submission.model;

import java.util.Map;

public record TraceCarrier(Map<String, String> headers) {

  public TraceCarrier {
    headers = Map.copyOf(headers);
  }
}
