import type { Routes } from '@angular/router';
import { guestGuard } from './core/auth/auth-guards';
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
    ],
  },
  {
    path: '**',
    title: 'Page not found | CodeClash',
    loadComponent: () => import('./features/not-found/not-found').then((m) => m.NotFound),
  },
];
