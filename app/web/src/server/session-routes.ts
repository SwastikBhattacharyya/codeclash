import express, { type Request, type Response, Router } from 'express';
import { clearSessionCookie, isSessionId, sessionIdFrom, setSessionCookie } from './cookies';
import { apiUrl } from './env';
import { sendError } from './errors';
import { bearer, callGateway, GATEWAY_TIMEOUT_MS, withoutSessionId } from './gateway';

const SESSIONS_PATH = '/api/v1/sessions';

type SignInBody = {
  login?: unknown;
  password?: unknown;
  rememberMe?: unknown;
};

export const sessionRouter = Router();

sessionRouter.use(express.json());

sessionRouter.post('/', async (req: Request, res: Response) => {
  const { login, password, rememberMe } = (req.body ?? {}) as SignInBody;
  const credentials =
    typeof login === 'string' && login.includes('@')
      ? { email: login, password }
      : { username: login, password };

  const upstream = await callGateway(res, SESSIONS_PATH, {
    method: 'POST',
    headers: { 'content-type': 'application/json', accept: 'application/json' },
    body: JSON.stringify(credentials),
  });
  if (!upstream) {
    return;
  }
  if (!upstream.ok) {
    await forward(upstream, res);
    return;
  }

  const session = await readJson(upstream, res);
  if (session === undefined) {
    return;
  }
  const newSessionId = (session as { id?: unknown } | null)?.id;
  if (!isSessionId(newSessionId)) {
    sendBadGateway(res);
    return;
  }

  const previousSessionId = sessionIdFrom(req);
  if (previousSessionId && previousSessionId !== newSessionId) {
    void revoke(previousSessionId);
  }
  setSessionCookie(req, res, newSessionId, rememberMe === true);
  res.status(upstream.status).json(withoutSessionId(session));
});

sessionRouter.get('/', async (req: Request, res: Response) => {
  const sessionId = sessionIdFrom(req);
  if (!sessionId) {
    sendError(res, 401, 'UNAUTHENTICATED', 'Not signed in', 'There is no active session');
    return;
  }

  const upstream = await callGateway(res, `${SESSIONS_PATH}/${sessionId}`, {
    headers: { authorization: bearer(sessionId), accept: 'application/json' },
  });
  if (!upstream) {
    return;
  }
  if (!upstream.ok) {
    if (upstream.status === 401 || upstream.status === 404) {
      clearSessionCookie(req, res);
    }
    await forward(upstream, res);
    return;
  }

  const session = await readJson(upstream, res);
  if (session === undefined) {
    return;
  }
  res.status(upstream.status).json(withoutSessionId(session));
});

sessionRouter.delete('/', async (req: Request, res: Response) => {
  const sessionId = sessionIdFrom(req);
  if (sessionId) {
    const upstream = await callGateway(res, `${SESSIONS_PATH}/${sessionId}`, {
      method: 'DELETE',
      headers: { authorization: bearer(sessionId), accept: 'application/json' },
    });
    if (!upstream) {
      return;
    }
    if (!upstream.ok && upstream.status !== 401 && upstream.status !== 404) {
      await forward(upstream, res);
      return;
    }
  }
  clearSessionCookie(req, res);
  res.status(204).end();
});

async function forward(upstream: globalThis.Response, res: Response): Promise<void> {
  const contentType = upstream.headers.get('content-type');
  if (contentType) {
    res.setHeader('content-type', contentType);
  }
  res.status(upstream.status).send(await upstream.text());
}

async function readJson(upstream: globalThis.Response, res: Response): Promise<unknown> {
  try {
    return await upstream.json();
  } catch {
    sendBadGateway(res);
    return undefined;
  }
}

function sendBadGateway(res: Response): void {
  sendError(
    res,
    502,
    'BAD_GATEWAY_RESPONSE',
    'Bad gateway response',
    'The API gateway sent a response that could not be read',
  );
}

async function revoke(sessionId: string): Promise<void> {
  try {
    await fetch(`${apiUrl()}${SESSIONS_PATH}/${sessionId}`, {
      method: 'DELETE',
      headers: { authorization: bearer(sessionId) },
      signal: AbortSignal.timeout(GATEWAY_TIMEOUT_MS),
    });
  } catch {
    return;
  }
}
