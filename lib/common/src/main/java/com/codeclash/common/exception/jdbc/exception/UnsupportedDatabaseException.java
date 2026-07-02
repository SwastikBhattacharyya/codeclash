package com.codeclash.common.exception.jdbc.exception;

import com.codeclash.common.exception.CodedException;
import com.codeclash.common.exception.CommonErrorCode;
import lombok.Getter;

@Getter
public class UnsupportedDatabaseException extends CodedException {

  private final String database;

  public UnsupportedDatabaseException(String database) {
    super(
        CommonErrorCode.UNSUPPORTED_DATABASE,
        "No constraint finder exists for database %s".formatted(database));
    this.database = database;
  }
}
