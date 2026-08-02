import type { Response } from 'express';
import { apiUrl } from './env';
import { sendError } from './errors';

export const GATEWAY_TIMEOUT_MS = 10_000;

export type GatewayRequestInit = RequestInit & { duplex?: 'half' };

const API_PATH_PREFIX = '/api/';
const SESSIONS_API_PATH = /^\/api\/v\d+\/sessions(?:[/?#]|$)/i;
const ENCODED_SEPARATOR_OR_DOT = /%2e|%2f|%5c|\\/i;

export type GatewayCallOptions = {
  abortOnClientClose: boolean;
};

const DEFAULT_CALL_OPTIONS: GatewayCallOptions = { abortOnClientClose: true };

export async function callGateway(
  res: Response,
  path: string,
  init: GatewayRequestInit,
  options: GatewayCallOptions = DEFAULT_CALL_OPTIONS,
): Promise<globalThis.Response | null> {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), GATEWAY_TIMEOUT_MS);
  let clientGone = false;
  res.on('close', () => {
    if (!res.writableFinished) {
      clientGone = true;
      if (options.abortOnClientClose) {
        controller.abort();
      }
    }
  });
  try {
    return await fetch(`${apiUrl()}${path}`, {
      ...init,
      redirect: 'manual',
      signal: controller.signal,
    });
  } catch {
    if (clientGone) {
      return null;
    }
    if (controller.signal.aborted) {
      sendError(
        res,
        504,
        'GATEWAY_TIMEOUT',
        'Gateway timeout',
        'The API gateway did not respond in time',
      );
    } else {
      sendError(
        res,
        503,
        'GATEWAY_UNAVAILABLE',
        'Gateway unavailable',
        'The API gateway could not be reached',
      );
    }
    return null;
  } finally {
    clearTimeout(timer);
  }
}

export function isSafeApiPath(originalUrl: string): boolean {
  const path = originalUrl.split('?', 1)[0] ?? '';
  if (!path.startsWith(API_PATH_PREFIX) || ENCODED_SEPARATOR_OR_DOT.test(path)) {
    return false;
  }
  return path.split('/').every((segment) => segment !== '.' && segment !== '..');
}

export function isSessionsApiPath(path: string): boolean {
  return SESSIONS_API_PATH.test(path);
}

export function bearer(sessionId: string): string {
  return `Bearer ${sessionId}`;
}

export function withoutSessionId(body: unknown): unknown {
  if (body === null || typeof body !== 'object') {
    return body;
  }
  const { id: _id, ...rest } = body as Record<string, unknown>;
  return rest;
}
