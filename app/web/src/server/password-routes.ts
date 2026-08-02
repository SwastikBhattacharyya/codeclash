import express, { type Request, type Response, Router } from 'express';
import { clearSessionCookie, sessionIdFrom } from './cookies';
import { apiUrl } from './env';
import { sendError } from './errors';
import { bearer, callGateway, GATEWAY_TIMEOUT_MS } from './gateway';

const SESSIONS_PATH = '/api/v1/sessions';
const USERS_PATH = '/api/v1/users';
const SESSION_PAGE_SIZE = 100;
const MAX_SESSION_PAGES = 20;
const REVOKE_CONCURRENCY = 10;
const DELETE_ATTEMPTS = 2;
const PASSWORD_MIN_LENGTH = 8;
const PASSWORD_MAX_LENGTH = 256;

type PasswordBody = {
  currentPassword?: unknown;
  newPassword?: unknown;
};

export type PasswordChangeResult = {
  otherSessionsSignedOut: boolean;
};

export const passwordRouter = Router();

passwordRouter.use(express.json());

passwordRouter.post('/', async (req: Request, res: Response) => {
  const sessionId = sessionIdFrom(req);
  if (!sessionId) {
    sendError(res, 401, 'UNAUTHENTICATED', 'Not signed in', 'There is no active session');
    return;
  }
  const { currentPassword, newPassword } = (req.body ?? {}) as PasswordBody;
  if (typeof currentPassword !== 'string' || currentPassword === '') {
    sendCurrentPasswordIncorrect(res);
    return;
  }
  if (typeof newPassword !== 'string' || newPassword === '') {
    sendError(res, 422, 'NULL_PASSWORD', 'Password is required', 'Password cannot be null');
    return;
  }
  const newPasswordLength = [...newPassword.normalize('NFKC')].length;
  if (newPasswordLength < PASSWORD_MIN_LENGTH || newPasswordLength > PASSWORD_MAX_LENGTH) {
    sendPasswordLength(res, newPasswordLength < PASSWORD_MIN_LENGTH);
    return;
  }

  const authorized = { authorization: bearer(sessionId), accept: 'application/json' };

  const session = await readJson<{ userId?: unknown }>(
    req,
    res,
    await callGateway(res, `${SESSIONS_PATH}/${sessionId}`, { headers: authorized }),
  );
  if (!session) {
    return;
  }
  const userId = session.userId;
  if (typeof userId !== 'string') {
    sendBadGateway(res);
    return;
  }

  const user = await readJson<{ username?: unknown }>(
    req,
    res,
    await callGateway(res, `${USERS_PATH}/${encodeURIComponent(userId)}`, { headers: authorized }),
  );
  if (!user) {
    return;
  }
  if (typeof user.username !== 'string') {
    sendBadGateway(res);
    return;
  }

  const check = await callGateway(
    res,
    SESSIONS_PATH,
    {
      method: 'POST',
      headers: { 'content-type': 'application/json', accept: 'application/json' },
      body: JSON.stringify({ username: user.username, password: currentPassword }),
    },
    { abortOnClientClose: false },
  );
  if (!check) {
    return;
  }
  if (check.status === 401) {
    sendCurrentPasswordIncorrect(res);
    return;
  }
  if (!check.ok) {
    await forward(check, res);
    return;
  }
  const throwaway = (await check.json().catch(() => null)) as { id?: unknown } | null;
  if (typeof throwaway?.id === 'string') {
    await deleteSession(throwaway.id);
  }

  const update = await callGateway(
    res,
    `${USERS_PATH}/${encodeURIComponent(userId)}`,
    {
      method: 'PATCH',
      headers: { ...authorized, 'content-type': 'application/json' },
      body: JSON.stringify({ password: newPassword }),
    },
    { abortOnClientClose: false },
  );
  if (!update) {
    await revokeOtherSessions(sessionId, userId);
    return;
  }
  if (!update.ok) {
    await forward(update, res);
    return;
  }

  const otherSessionsSignedOut = await revokeOtherSessions(sessionId, userId);
  const result: PasswordChangeResult = { otherSessionsSignedOut };
  res.status(200).json(result);
});

async function readJson<T>(
  req: Request,
  res: Response,
  upstream: globalThis.Response | null,
): Promise<T | null> {
  if (!upstream) {
    return null;
  }
  if (!upstream.ok) {
    if (upstream.status === 401) {
      clearSessionCookie(req, res);
    }
    await forward(upstream, res);
    return null;
  }
  const body = (await upstream.json().catch(() => null)) as T | null;
  if (body === null) {
    sendBadGateway(res);
  }
  return body;
}

async function revokeOtherSessions(currentSessionId: string, userId: string): Promise<boolean> {
  const others: string[] = [];
  let listedAll = false;
  for (let page = 1; page <= MAX_SESSION_PAGES; page += 1) {
    const query = new URLSearchParams({
      user_id: userId,
      page: String(page),
      size: String(SESSION_PAGE_SIZE),
    });
    const listed = await gatewayFetch(`${SESSIONS_PATH}?${query}`, {
      headers: { authorization: bearer(currentSessionId), accept: 'application/json' },
    });
    if (!listed?.ok) {
      break;
    }
    const body = (await listed.json().catch(() => null)) as {
      data?: { id?: unknown }[];
      hasNext?: unknown;
    } | null;
    if (!body) {
      break;
    }
    for (const item of body.data ?? []) {
      if (typeof item.id === 'string' && item.id !== currentSessionId) {
        others.push(item.id);
      }
    }
    if (body.hasNext !== true) {
      listedAll = true;
      break;
    }
  }

  let allDeleted = true;
  for (let start = 0; start < others.length; start += REVOKE_CONCURRENCY) {
    const batch = others.slice(start, start + REVOKE_CONCURRENCY);
    const results = await Promise.all(batch.map((id) => deleteSession(id)));
    if (results.includes(false)) {
      allDeleted = false;
    }
  }
  if (!listedAll || !allDeleted) {
    console.error(`Could not sign out every other session of user ${userId}`);
  }
  return listedAll && allDeleted;
}

async function deleteSession(sessionId: string): Promise<boolean> {
  for (let attempt = 0; attempt < DELETE_ATTEMPTS; attempt += 1) {
    const response = await gatewayFetch(`${SESSIONS_PATH}/${sessionId}`, {
      method: 'DELETE',
      headers: { authorization: bearer(sessionId) },
    });
    if (response && (response.ok || response.status === 401 || response.status === 404)) {
      return true;
    }
  }
  return false;
}

async function gatewayFetch(path: string, init: RequestInit): Promise<globalThis.Response | null> {
  try {
    return await fetch(`${apiUrl()}${path}`, {
      ...init,
      signal: AbortSignal.timeout(GATEWAY_TIMEOUT_MS),
    });
  } catch {
    return null;
  }
}

async function forward(upstream: globalThis.Response, res: Response): Promise<void> {
  res.status(upstream.status).type(upstream.headers.get('content-type') ?? 'application/json');
  res.send(await upstream.text());
}

function sendPasswordLength(res: Response, tooShort: boolean): void {
  res.status(422).json({
    code: tooShort ? 'PASSWORD_TOO_SHORT' : 'PASSWORD_TOO_LONG',
    title: tooShort ? 'Password too short' : 'Password too long',
    message: tooShort
      ? `Password must contain at least ${PASSWORD_MIN_LENGTH} characters`
      : `Password must contain at most ${PASSWORD_MAX_LENGTH} characters`,
    timestamp: new Date().toISOString(),
    properties: { min: PASSWORD_MIN_LENGTH, max: PASSWORD_MAX_LENGTH },
  });
}

function sendCurrentPasswordIncorrect(res: Response): void {
  sendError(
    res,
    422,
    'CURRENT_PASSWORD_INCORRECT',
    'Current password is incorrect',
    'The current password is not correct',
  );
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
