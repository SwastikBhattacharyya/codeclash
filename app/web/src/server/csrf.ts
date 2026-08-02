import type { NextFunction, Request, Response } from 'express';
import { sendError } from './errors';

const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS']);

export function rejectCrossSite(req: Request, res: Response, next: NextFunction): void {
  if (SAFE_METHODS.has(req.method)) {
    next();
    return;
  }
  const origin = req.get('origin');
  if (
    req.get('sec-fetch-site') === 'cross-site' ||
    (origin !== undefined && !isSameOrigin(origin, req))
  ) {
    sendError(
      res,
      403,
      'CROSS_SITE_REQUEST',
      'Cross-site request',
      'Requests that change data must come from this site',
    );
    return;
  }
  next();
}

function isSameOrigin(origin: string, req: Request): boolean {
  const ownOrigin = `${req.protocol}://${req.host}`;
  return (
    URL.canParse(origin) &&
    URL.canParse(ownOrigin) &&
    new URL(origin).origin === new URL(ownOrigin).origin
  );
}
