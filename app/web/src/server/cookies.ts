import type { CookieOptions, Request, Response } from 'express';

export const SESSION_COOKIE = 'cc_session';
export const REMEMBER_ME_MAX_AGE_MS = 30 * 24 * 60 * 60 * 1000;

const SESSION_ID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export function isSessionId(value: unknown): value is string {
  return typeof value === 'string' && SESSION_ID_PATTERN.test(value);
}

export function readSessionId(cookieHeader: string | null | undefined): string | null {
  if (!cookieHeader) {
    return null;
  }
  for (const part of cookieHeader.split(';')) {
    const separator = part.indexOf('=');
    if (separator === -1 || part.slice(0, separator).trim() !== SESSION_COOKIE) {
      continue;
    }
    const value = part.slice(separator + 1).trim();
    if (isSessionId(value)) {
      return value;
    }
  }
  return null;
}

export function sessionIdFrom(req: Request): string | null {
  return readSessionId(req.headers.cookie);
}

export function setSessionCookie(
  req: Request,
  res: Response,
  sessionId: string,
  rememberMe: boolean,
): void {
  const options = baseOptions(req);
  res.cookie(
    SESSION_COOKIE,
    sessionId,
    rememberMe ? { ...options, maxAge: REMEMBER_ME_MAX_AGE_MS } : options,
  );
}

export function clearSessionCookie(req: Request, res: Response): void {
  res.clearCookie(SESSION_COOKIE, baseOptions(req));
}

function baseOptions(req: Request): CookieOptions {
  return { httpOnly: true, sameSite: 'lax', secure: req.secure, path: '/' };
}
