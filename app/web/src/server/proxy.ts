import { Readable } from 'node:stream';
import { pipeline } from 'node:stream/promises';
import type { ReadableStream } from 'node:stream/web';
import type { Request, Response } from 'express';
import { clearSessionCookie, sessionIdFrom } from './cookies';
import { apiUrl } from './env';
import { sendError } from './errors';
import { bearer, callGateway, isSafeApiPath, isSessionsApiPath } from './gateway';

const FORWARDED_REQUEST_HEADERS = [
  'content-type',
  'accept',
  'accept-language',
  'x-email-verification-token',
];

const FORWARDED_RESPONSE_HEADERS = ['content-type', 'retry-after'];

const METHODS_WITHOUT_BODY = new Set(['GET', 'HEAD']);

export async function proxyToGateway(req: Request, res: Response): Promise<void> {
  if (!isSafeApiPath(req.originalUrl)) {
    sendError(res, 400, 'INVALID_PATH', 'Invalid path', 'The request path is not allowed');
    return;
  }
  if (isSessionsApiPath(req.originalUrl)) {
    sendError(res, 404, 'NOT_FOUND', 'Not found', 'Sessions are managed through /bff/session');
    return;
  }

  const headers = new Headers();
  for (const name of FORWARDED_REQUEST_HEADERS) {
    const value = req.get(name);
    if (value) {
      headers.set(name, value);
    }
  }
  const sessionId = sessionIdFrom(req);
  if (sessionId) {
    headers.set('authorization', bearer(sessionId));
  }

  const hasBody = !METHODS_WITHOUT_BODY.has(req.method);
  const upstream = await callGateway(res, req.originalUrl, {
    method: req.method,
    headers,
    body: hasBody ? (Readable.toWeb(req) as globalThis.ReadableStream) : undefined,
    duplex: hasBody ? 'half' : undefined,
  });
  if (!upstream) {
    return;
  }

  if (upstream.status === 401 && sessionId) {
    clearSessionCookie(req, res);
  }
  res.status(upstream.status);
  for (const name of FORWARDED_RESPONSE_HEADERS) {
    const value = upstream.headers.get(name);
    if (value) {
      res.setHeader(name, value);
    }
  }
  const location = upstream.headers.get('location');
  if (location) {
    res.setHeader('location', toPublicLocation(location));
  }

  if (!upstream.body) {
    res.end();
    return;
  }
  try {
    await pipeline(Readable.fromWeb(upstream.body as ReadableStream), res);
  } catch {
    res.destroy();
  }
}

function toPublicLocation(location: string): string {
  if (!URL.canParse(location)) {
    return location;
  }
  const target = new URL(location);
  if (target.origin !== new URL(apiUrl()).origin) {
    return location;
  }
  return `${target.pathname}${target.search}${target.hash}`;
}
