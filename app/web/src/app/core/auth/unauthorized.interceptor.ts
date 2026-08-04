import { HttpErrorResponse, type HttpInterceptorFn, HttpStatusCode } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ACCOUNT_PREFIX, REDIRECT_PARAM, SIGN_IN_PATH } from './auth-paths';
import { AuthStore } from './auth-store';
import { SESSION_URL } from './session';

export const unauthorizedInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url === SESSION_URL) {
    return next(req);
  }
  const store = inject(AuthStore);
  const router = inject(Router);
  return next(req).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === HttpStatusCode.Unauthorized) {
        store.clear();
        const current = router.url;
        if (current === ACCOUNT_PREFIX || current.startsWith(`${ACCOUNT_PREFIX}/`)) {
          void router.navigate([SIGN_IN_PATH], { queryParams: { [REDIRECT_PARAM]: current } });
        }
      }
      return throwError(() => error);
    }),
  );
};
