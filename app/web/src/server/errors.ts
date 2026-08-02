import type { NextFunction, Request, Response } from 'express';

export function sendError(
  res: Response,
  status: number,
  code: string,
  title: string,
  message: string,
): void {
  res.status(status).json({ code, title, message, timestamp: new Date().toISOString() });
}

export function noStore(_req: Request, res: Response, next: NextFunction): void {
  res.setHeader('cache-control', 'private, no-store');
  next();
}

export function handleError(err: unknown, req: Request, res: Response, next: NextFunction): void {
  if (res.headersSent) {
    next(err);
    return;
  }
  const type = typeof err === 'object' && err !== null ? (err as { type?: unknown }).type : null;
  if (type === 'entity.parse.failed') {
    sendError(
      res,
      400,
      'MALFORMED_REQUEST_BODY',
      'Malformed request body',
      'The request body is not valid JSON',
    );
    return;
  }
  if (type === 'entity.too.large') {
    sendError(
      res,
      413,
      'REQUEST_BODY_TOO_LARGE',
      'Request body too large',
      'The request body is too large',
    );
    return;
  }
  console.error(`Unhandled error on ${req.method} ${req.originalUrl}`, err);
  sendError(res, 500, 'INTERNAL_ERROR', 'Internal error', 'Something went wrong');
}
