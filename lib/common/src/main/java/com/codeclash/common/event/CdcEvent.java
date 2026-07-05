package com.codeclash.common.event;

public record CdcEvent<T>(Operation operation, T data) {}
