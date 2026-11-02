import { inject } from '@angular/core';
import { type CanActivateFn, type CanMatchFn, Router } from '@angular/router';
import { REDIRECT_PARAM, SIGN_IN_PATH } from '../auth/auth-paths';
import { AuthStore } from '../auth/auth-store';
import { ADMIN_PATH, hasAdminAccess } from './admin-access';

export const adminMatchGuard: CanMatchFn = () => {
  const session = inject(AuthStore).session();
  return session === null || hasAdminAccess(session.permissions);
};

export const adminGuard: CanActivateFn = (_route, state) => {
  if (inject(AuthStore).isAuthenticated()) {
    return true;
  }
  return inject(Router).createUrlTree([SIGN_IN_PATH], {
    queryParams: { [REDIRECT_PARAM]: state.url },
  });
};

export function sectionGuard(permission: string): CanActivateFn {
  return () => {
    const permissions = inject(AuthStore).session()?.permissions ?? [];
    if (permissions.includes(permission)) {
      return true;
    }
    return inject(Router).createUrlTree([ADMIN_PATH]);
  };
}
