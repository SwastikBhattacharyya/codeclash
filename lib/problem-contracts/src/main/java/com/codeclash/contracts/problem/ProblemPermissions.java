package com.codeclash.contracts.problem;

import com.codeclash.common.permission.Description;
import com.codeclash.common.permission.Name;
import com.codeclash.common.permission.PermissionDefinition;
import com.codeclash.common.permission.PermissionList;
import java.util.List;

public final class ProblemPermissions implements PermissionList {

  public static final String TAG_WRITE = "problem:tag:write";
  public static final String CATEGORY_WRITE = "problem:category:write";
  public static final String QUESTION_WRITE = "problem:question:write";
  public static final String QUESTION_READ_IS_PRIVATE = "problem:question:read_is_private";
  public static final String QUESTION_WRITE_IS_PRIVATE = "problem:question:write_is_private";
  public static final String TEST_CASE_WRITE = "problem:test_case:write";
  public static final String TEST_CASE_READ_IS_PRIVATE = "problem:test_case:read_is_private";
  public static final String TEST_CASE_WRITE_IS_PRIVATE = "problem:test_case:write_is_private";
  public static final String SUBMISSION_WRITE = "problem:submission:write";
  public static final String SUBMISSION_DELETE = "problem:submission:delete";
  public static final String SUBMISSION_DELETE_OWN = "problem:submission:delete_own";

  @Override
  public List<PermissionDefinition> permissions() {
    return List.of(
        ProblemPermissions.definition(
            ProblemPermissions.TAG_WRITE, "Create, replace, patch and delete tags"),
        ProblemPermissions.definition(
            ProblemPermissions.CATEGORY_WRITE, "Create, replace, patch and delete categories"),
        ProblemPermissions.definition(
            ProblemPermissions.QUESTION_WRITE, "Create, replace, patch and delete questions"),
        ProblemPermissions.definition(
            ProblemPermissions.QUESTION_READ_IS_PRIVATE, "Read private questions"),
        ProblemPermissions.definition(
            ProblemPermissions.QUESTION_WRITE_IS_PRIVATE, "Write private questions"),
        ProblemPermissions.definition(
            ProblemPermissions.TEST_CASE_WRITE, "Create, replace, patch and delete test cases"),
        ProblemPermissions.definition(
            ProblemPermissions.TEST_CASE_READ_IS_PRIVATE, "Read private test cases"),
        ProblemPermissions.definition(
            ProblemPermissions.TEST_CASE_WRITE_IS_PRIVATE, "Write private test cases"),
        ProblemPermissions.definition(ProblemPermissions.SUBMISSION_WRITE, "Create submissions"),
        ProblemPermissions.definition(
            ProblemPermissions.SUBMISSION_DELETE, "Delete other users' submissions"),
        ProblemPermissions.definition(
            ProblemPermissions.SUBMISSION_DELETE_OWN, "Delete your own submissions"));
  }

  private static PermissionDefinition definition(String name, String description) {
    return new PermissionDefinition(Name.of(name), Description.of(description));
  }
}
