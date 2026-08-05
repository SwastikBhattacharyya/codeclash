import { inject } from '@angular/core';
import { type CanActivateFn, Router } from '@angular/router';
import { PROFILE_PATH, REDIRECT_PARAM, SIGN_IN_PATH } from './auth-paths';
import { AuthStore } from './auth-store';

export const authGuard: CanActivateFn = (_route, state) => {
  if (inject(AuthStore).isAuthenticated()) {
    return true;
  }
  return inject(Router).createUrlTree([SIGN_IN_PATH], {
    queryParams: { [REDIRECT_PARAM]: state.url },
  });
};

export const guestGuard: CanActivateFn = () => {
  if (!inject(AuthStore).isAuthenticated()) {
    return true;
  }
  return inject(Router).createUrlTree([PROFILE_PATH]);
};
