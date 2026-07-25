package com.codeclash.gatewayservice.rest.adapter.inbound.rest;

import com.codeclash.common.exception.ErrorCode;

public enum GatewayErrorCode implements ErrorCode {
  GATEWAY_TIMEOUT("Gateway timeout");

  private final String title;

  GatewayErrorCode(String title) {
    this.title = title;
  }

  @Override
  public String title() {
    return this.title;
  }
}
