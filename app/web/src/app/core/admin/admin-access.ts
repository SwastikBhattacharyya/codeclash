export const ADMIN_PATH = '/admin';

export const QUESTION_WRITE = 'problem:question:write';
export const TEST_CASE_WRITE = 'problem:test_case:write';
export const TEST_CASE_WRITE_IS_PRIVATE = 'problem:test_case:write_is_private';
export const TEST_CASE_READ_IS_PRIVATE = 'problem:test_case:read_is_private';
export const TAG_WRITE = 'problem:tag:write';
export const CATEGORY_WRITE = 'problem:category:write';
export const SUBMISSION_DELETE = 'problem:submission:delete';
export const CONTEST_WRITE = 'contest:contest:write';
export const CONTEST_READ_QUESTIONS = 'contest:contest:read_questions';
export const QUESTION_READ_IS_PRIVATE = 'problem:question:read_is_private';
export const USER_WRITE = 'auth:user:write';
export const USER_WRITE_OWN = 'auth:user:write_own';
export const USER_WRITE_USERNAME = 'auth:user:write_username';
export const USER_WRITE_USERNAME_OWN = 'auth:user:write_username_own';
export const USER_WRITE_ROLE = 'auth:user:write_role';
export const USER_WRITE_ROLE_OWN = 'auth:user:write_role_own';
export const USER_WRITE_IS_VERIFIED = 'auth:user:write_is_verified';
export const USER_WRITE_IS_VERIFIED_OWN = 'auth:user:write_is_verified_own';
export const USER_READ_EMAIL = 'auth:user:read_email';
export const USER_READ_ROLE = 'auth:user:read_role';
export const ROLE_READ = 'auth:role:read';
export const ROLE_WRITE = 'auth:role:write';
export const PERMISSION_READ = 'auth:permission:read';

export const ADMIN_PERMISSIONS: readonly string[] = [
  QUESTION_WRITE,
  TAG_WRITE,
  CATEGORY_WRITE,
  SUBMISSION_DELETE,
  CONTEST_WRITE,
  USER_WRITE,
  ROLE_READ,
];

export function hasAdminAccess(permissions: readonly string[]): boolean {
  return ADMIN_PERMISSIONS.some((permission) => permissions.includes(permission));
}
