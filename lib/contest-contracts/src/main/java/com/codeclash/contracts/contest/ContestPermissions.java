package com.codeclash.contracts.contest;

import com.codeclash.common.permission.Description;
import com.codeclash.common.permission.Name;
import com.codeclash.common.permission.PermissionDefinition;
import com.codeclash.common.permission.PermissionList;
import java.util.List;

public final class ContestPermissions implements PermissionList {

  public static final String CONTEST_WRITE = "contest:contest:write";
  public static final String CONTEST_READ_QUESTIONS = "contest:contest:read_questions";

  @Override
  public List<PermissionDefinition> permissions() {
    return List.of(
        ContestPermissions.definition(
            ContestPermissions.CONTEST_WRITE, "Create, replace, patch and delete contests"),
        ContestPermissions.definition(
            ContestPermissions.CONTEST_READ_QUESTIONS,
            "Read contest questions before the contest goes live"));
  }

  private static PermissionDefinition definition(String name, String description) {
    return new PermissionDefinition(Name.of(name), Description.of(description));
  }
}
