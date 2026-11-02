import {
  ADMIN_PATH,
  CATEGORY_WRITE,
  CONTEST_WRITE,
  QUESTION_WRITE,
  ROLE_READ,
  SUBMISSION_DELETE,
  TAG_WRITE,
  USER_WRITE,
} from './admin-access';

export type AdminGroup = 'Problems' | 'Contests' | 'Access';

export type AdminSectionKey =
  | 'questions'
  | 'tags'
  | 'categories'
  | 'submissions'
  | 'contests'
  | 'users'
  | 'roles';

export type AdminSection = {
  key: AdminSectionKey;
  group: AdminGroup;
  label: string;
  path: string;
  permission: string;
  description: string;
};

export type AdminSectionGroup = {
  label: AdminGroup;
  sections: AdminSection[];
};

export const ADMIN_GROUPS: readonly AdminGroup[] = ['Problems', 'Contests', 'Access'];

export const ADMIN_SECTIONS: readonly AdminSection[] = [
  {
    group: 'Problems',
    key: 'questions',
    label: 'Questions',
    path: `${ADMIN_PATH}/questions`,
    permission: QUESTION_WRITE,
    description: 'Problem statements, limits, visibility and test cases.',
  },
  {
    group: 'Problems',
    key: 'tags',
    label: 'Tags',
    path: `${ADMIN_PATH}/tags`,
    permission: TAG_WRITE,
    description: 'Labels that describe the techniques a problem uses.',
  },
  {
    group: 'Problems',
    key: 'categories',
    label: 'Categories',
    path: `${ADMIN_PATH}/categories`,
    permission: CATEGORY_WRITE,
    description: 'Broad topics that group problems together.',
  },
  {
    group: 'Problems',
    key: 'submissions',
    label: 'Submissions',
    path: `${ADMIN_PATH}/submissions`,
    permission: SUBMISSION_DELETE,
    description: 'Every submission made on the platform, with its verdict.',
  },
  {
    group: 'Contests',
    key: 'contests',
    label: 'Contests',
    path: `${ADMIN_PATH}/contests`,
    permission: CONTEST_WRITE,
    description: 'Schedules, problem sets and points for each contest.',
  },
  {
    group: 'Access',
    key: 'users',
    label: 'Users',
    path: `${ADMIN_PATH}/users`,
    permission: USER_WRITE,
    description: 'Accounts, verification status and assigned roles.',
  },
  {
    group: 'Access',
    key: 'roles',
    label: 'Roles',
    path: `${ADMIN_PATH}/roles`,
    permission: ROLE_READ,
    description: 'Named sets of permissions that can be given to users.',
  },
];

export function adminSection(key: AdminSectionKey): AdminSection {
  const section = ADMIN_SECTIONS.find((candidate) => candidate.key === key);
  if (section === undefined) {
    throw new Error(`Unknown admin section: ${key}`);
  }
  return section;
}

export function allowedSections(permissions: readonly string[]): AdminSection[] {
  return ADMIN_SECTIONS.filter((section) => permissions.includes(section.permission));
}

export function allowedGroups(permissions: readonly string[]): AdminSectionGroup[] {
  const sections = allowedSections(permissions);
  return ADMIN_GROUPS.map((label) => ({
    label,
    sections: sections.filter((section) => section.group === label),
  })).filter((group) => group.sections.length > 0);
}
