import type { Routes } from '@angular/router';
import {
  CATEGORY_WRITE,
  CONTEST_WRITE,
  QUESTION_WRITE,
  ROLE_READ,
  ROLE_WRITE,
  SUBMISSION_DELETE,
  TAG_WRITE,
  USER_WRITE,
} from './core/admin/admin-access';
import { adminGuard, adminMatchGuard, sectionGuard } from './core/admin/admin-guards';
import { authGuard, guestGuard } from './core/auth/auth-guards';
import { contestUnsavedGuard } from './features/admin/contests/contest-unsaved.guard';
import {
  CATEGORY_LOOKUP,
  LOOKUP_DATA_KEY,
  TAG_LOOKUP,
} from './features/admin/lookup/lookup-config';
import { questionUnsavedGuard } from './features/admin/questions/question-unsaved.guard';
import { roleUnsavedGuard } from './features/admin/roles/role-unsaved.guard';
import { AppLayout, FULL_WIDTH_DATA_KEY } from './layouts/app-layout';
import { AuthLayout } from './layouts/auth-layout';

export const routes: Routes = [
  {
    path: 'auth',
    component: AuthLayout,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'sign-in' },
      {
        path: 'sign-up',
        title: 'Sign up | CodeClash',
        canActivate: [guestGuard],
        loadComponent: () => import('./features/auth/sign-up/sign-up').then((m) => m.SignUp),
      },
      {
        path: 'sign-in',
        title: 'Sign in | CodeClash',
        canActivate: [guestGuard],
        loadComponent: () => import('./features/auth/sign-in/sign-in').then((m) => m.SignIn),
      },
      {
        path: 'verify-email',
        title: 'Verify email | CodeClash',
        loadComponent: () =>
          import('./features/auth/verify-email/verify-email').then((m) => m.VerifyEmail),
      },
    ],
  },
  {
    path: 'admin',
    loadComponent: () => import('./layouts/admin-layout').then((m) => m.AdminLayout),
    canMatch: [adminMatchGuard],
    canActivate: [adminGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        title: 'Overview | Admin | CodeClash',
        loadComponent: () =>
          import('./features/admin/overview/overview').then((m) => m.AdminOverview),
      },
      {
        path: 'questions',
        title: 'Questions | Admin | CodeClash',
        canActivate: [sectionGuard(QUESTION_WRITE)],
        loadComponent: () =>
          import('./features/admin/questions/questions-page').then((m) => m.QuestionsPage),
      },
      {
        path: 'questions/new',
        title: 'New question | Admin | CodeClash',
        canActivate: [sectionGuard(QUESTION_WRITE)],
        canDeactivate: [questionUnsavedGuard],
        loadComponent: () =>
          import('./features/admin/questions/question-detail').then((m) => m.QuestionDetail),
      },
      {
        path: 'questions/:id',
        title: 'Question | Admin | CodeClash',
        canActivate: [sectionGuard(QUESTION_WRITE)],
        canDeactivate: [questionUnsavedGuard],
        loadComponent: () =>
          import('./features/admin/questions/question-detail').then((m) => m.QuestionDetail),
      },
      {
        path: 'tags',
        title: 'Tags | Admin | CodeClash',
        canActivate: [sectionGuard(TAG_WRITE)],
        data: { [LOOKUP_DATA_KEY]: TAG_LOOKUP },
        loadComponent: () =>
          import('./features/admin/lookup/lookup-page').then((m) => m.LookupPage),
      },
      {
        path: 'categories',
        title: 'Categories | Admin | CodeClash',
        canActivate: [sectionGuard(CATEGORY_WRITE)],
        data: { [LOOKUP_DATA_KEY]: CATEGORY_LOOKUP },
        loadComponent: () =>
          import('./features/admin/lookup/lookup-page').then((m) => m.LookupPage),
      },
      {
        path: 'submissions',
        title: 'Submissions | Admin | CodeClash',
        canActivate: [sectionGuard(SUBMISSION_DELETE)],
        loadComponent: () =>
          import('./features/admin/submissions/submissions-page').then((m) => m.SubmissionsPage),
      },
      {
        path: 'contests',
        title: 'Contests | Admin | CodeClash',
        canActivate: [sectionGuard(CONTEST_WRITE)],
        loadComponent: () =>
          import('./features/admin/contests/contests-page').then((m) => m.ContestsPage),
      },
      {
        path: 'contests/new',
        title: 'New contest | Admin | CodeClash',
        canActivate: [sectionGuard(CONTEST_WRITE)],
        canDeactivate: [contestUnsavedGuard],
        loadComponent: () =>
          import('./features/admin/contests/contest-detail').then((m) => m.ContestDetail),
      },
      {
        path: 'contests/:id',
        title: 'Contest | Admin | CodeClash',
        canActivate: [sectionGuard(CONTEST_WRITE)],
        canDeactivate: [contestUnsavedGuard],
        loadComponent: () =>
          import('./features/admin/contests/contest-detail').then((m) => m.ContestDetail),
      },
      {
        path: 'users',
        title: 'Users | Admin | CodeClash',
        canActivate: [sectionGuard(USER_WRITE)],
        loadComponent: () => import('./features/admin/users/users-page').then((m) => m.UsersPage),
      },
      {
        path: 'roles',
        title: 'Roles | Admin | CodeClash',
        canActivate: [sectionGuard(ROLE_READ)],
        loadComponent: () => import('./features/admin/roles/roles-page').then((m) => m.RolesPage),
      },
      {
        path: 'roles/new',
        title: 'New role | Admin | CodeClash',
        runGuardsAndResolvers: 'paramsOrQueryParamsChange',
        canActivate: [sectionGuard(ROLE_WRITE)],
        canDeactivate: [roleUnsavedGuard],
        loadComponent: () => import('./features/admin/roles/role-detail').then((m) => m.RoleDetail),
      },
      {
        path: 'roles/:id',
        title: 'Role | Admin | CodeClash',
        canActivate: [sectionGuard(ROLE_READ)],
        canDeactivate: [roleUnsavedGuard],
        loadComponent: () => import('./features/admin/roles/role-detail').then((m) => m.RoleDetail),
      },
    ],
  },
  {
    path: '',
    component: AppLayout,
    children: [
      {
        path: '',
        pathMatch: 'full',
        title: 'Problems | CodeClash',
        loadComponent: () =>
          import('./features/problems/problem-list/problem-list').then((m) => m.ProblemList),
      },
      {
        path: 'problems/:id',
        title: 'Problem | CodeClash',
        data: { [FULL_WIDTH_DATA_KEY]: true },
        loadComponent: () =>
          import('./features/problems/problem-detail/problem-detail').then((m) => m.ProblemDetail),
      },
      {
        path: 'contests',
        title: 'Contests | CodeClash',
        loadComponent: () =>
          import('./features/contests/contest-list/contest-list').then((m) => m.ContestList),
      },
      {
        path: 'contests/:id',
        title: 'Contest | CodeClash',
        loadComponent: () =>
          import('./features/contests/contest-detail/contest-detail').then((m) => m.ContestDetail),
      },
      {
        path: 'account',
        canActivateChild: [authGuard],
        children: [
          { path: '', pathMatch: 'full', redirectTo: 'profile' },
          {
            path: 'profile',
            title: 'Profile | CodeClash',
            loadComponent: () =>
              import('./features/account/profile/profile').then((m) => m.Profile),
          },
          {
            path: 'password',
            title: 'Change password | CodeClash',
            loadComponent: () =>
              import('./features/account/change-password/change-password').then(
                (m) => m.ChangePassword,
              ),
          },
        ],
      },
    ],
  },
  {
    path: '**',
    title: 'Page not found | CodeClash',
    loadComponent: () => import('./features/not-found/not-found').then((m) => m.NotFound),
  },
];
