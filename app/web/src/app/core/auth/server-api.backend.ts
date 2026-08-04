import {
  FetchBackend,
  HttpErrorResponse,
  type HttpEvent,
  type HttpRequest,
  HttpResponse,
  HttpStatusCode,
} from '@angular/common/http';
import { Injectable, inject, REQUEST } from '@angular/core';
import { map, type Observable, throwError } from 'rxjs';
import { readSessionId } from '../../../server/cookies';
import { apiUrl } from '../../../server/env';
import {
  bearer,
  GATEWAY_TIMEOUT_MS,
  isSessionsApiPath,
  withoutSessionId,
} from '../../../server/gateway';
import { SESSION_URL } from './session';

const API_PREFIX = '/api/';
const SESSIONS_PATH = '/api/v1/sessions';

@Injectable()
export class ServerApiBackend extends FetchBackend {
  private readonly request = inject(REQUEST);

  override handle(req: HttpRequest<unknown>): Observable<HttpEvent<unknown>> {
    const sessionId = readSessionId(this.request?.headers.get('cookie'));
    const path = this.ownPath(req.url);

    if (path === SESSION_URL && req.method === 'GET') {
      if (!sessionId) {
        return throwError(
          () =>
            new HttpErrorResponse({
              status: HttpStatusCode.Unauthorized,
              statusText: 'Unauthorized',
              url: req.url,
            }),
        );
      }
      const upstream = req.clone({
        url: `${apiUrl()}${SESSIONS_PATH}/${sessionId}`,
        setHeaders: { authorization: bearer(sessionId) },
        timeout: GATEWAY_TIMEOUT_MS,
      });
      return super
        .handle(upstream)
        .pipe(
          map((event) =>
            event instanceof HttpResponse
              ? event.clone({ body: withoutSessionId(event.body), url: SESSION_URL })
              : event,
          ),
        );
    }

    if (path?.startsWith(API_PREFIX)) {
      if (isSessionsApiPath(path)) {
        return throwError(
          () =>
            new HttpErrorResponse({
              status: HttpStatusCode.NotFound,
              statusText: 'Not Found',
              url: req.url,
            }),
        );
      }
      return super.handle(
        req.clone({
          url: `${apiUrl()}${path}`,
          setHeaders: sessionId ? { authorization: bearer(sessionId) } : {},
          timeout: GATEWAY_TIMEOUT_MS,
        }),
      );
    }

    return super.handle(req);
  }

  private ownPath(url: string): string | null {
    if (url.startsWith('/')) {
      return url;
    }
    if (!this.request) {
      return null;
    }
    const origin = new URL(this.request.url).origin;
    return url.startsWith(`${origin}/`) ? url.slice(origin.length) : null;
  }
}
