import express, { type Request, type Response, Router } from 'express';
import { sendError } from './errors';
import { callGateway } from './gateway';

const USERS_PATH = '/api/v1/users';
const VERIFICATION_TOKEN_HEADER = 'x-email-verification-token';
const LOOKUP_PAGE_SIZE = 100;
const MAX_LOOKUP_PAGES = 20;
const TOKEN_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const VERIFICATION_REJECTIONS = new Set([400, 401, 404, 409, 422]);

type VerifyBody = {
  username?: unknown;
  token?: unknown;
};

type UserSummary = {
  id?: unknown;
  username?: unknown;
  isVerified?: unknown;
};

type UserPage = {
  data?: UserSummary[];
  hasNext?: unknown;
};

type Lookup = { kind: 'found'; user: UserSummary } | { kind: 'missing' } | { kind: 'sent' };

export const verifyRouter = Router();

verifyRouter.use(express.json());

verifyRouter.post('/', async (req: Request, res: Response) => {
  const { username, token } = (req.body ?? {}) as VerifyBody;
  if (typeof username !== 'string' || username.trim() === '' || typeof token !== 'string') {
    sendVerificationFailed(res);
    return;
  }
  const trimmedUsername = username.trim();
  const trimmedToken = token.trim();
  if (!TOKEN_PATTERN.test(trimmedToken)) {
    sendVerificationFailed(res);
    return;
  }

  const lookup = await findUser(res, trimmedUsername);
  if (lookup.kind === 'sent') {
    return;
  }
  if (lookup.kind === 'missing' || typeof lookup.user.id !== 'string') {
    sendVerificationFailed(res);
    return;
  }
  if (lookup.user.isVerified === true) {
    res.status(204).end();
    return;
  }

  const upstream = await callGateway(
    res,
    `${USERS_PATH}/${encodeURIComponent(lookup.user.id)}`,
    {
      method: 'PATCH',
      headers: {
        'content-type': 'application/json',
        accept: 'application/json',
        [VERIFICATION_TOKEN_HEADER]: trimmedToken,
      },
      body: JSON.stringify({ verified: true }),
    },
    { abortOnClientClose: false },
  );
  if (!upstream) {
    return;
  }
  if (upstream.ok) {
    res.status(204).end();
    return;
  }
  if (VERIFICATION_REJECTIONS.has(upstream.status)) {
    sendVerificationFailed(res);
    return;
  }
  await forward(upstream, res);
});

async function findUser(res: Response, username: string): Promise<Lookup> {
  for (let page = 1; page <= MAX_LOOKUP_PAGES; page += 1) {
    const query = new URLSearchParams({
      username,
      page: String(page),
      size: String(LOOKUP_PAGE_SIZE),
    });
    const upstream = await callGateway(res, `${USERS_PATH}?${query}`, {
      headers: { accept: 'application/json' },
    });
    if (!upstream) {
      return { kind: 'sent' };
    }
    if (!upstream.ok) {
      if (VERIFICATION_REJECTIONS.has(upstream.status)) {
        return { kind: 'missing' };
      }
      await forward(upstream, res);
      return { kind: 'sent' };
    }
    const body = (await upstream.json().catch(() => null)) as UserPage | null;
    const user = body?.data?.find((candidate) => candidate.username === username);
    if (user) {
      return { kind: 'found', user };
    }
    if (body?.hasNext !== true) {
      return { kind: 'missing' };
    }
  }
  return { kind: 'missing' };
}

async function forward(upstream: globalThis.Response, res: Response): Promise<void> {
  res.status(upstream.status).type(upstream.headers.get('content-type') ?? 'application/json');
  res.send(await upstream.text());
}

function sendVerificationFailed(res: Response): void {
  sendError(
    res,
    422,
    'VERIFICATION_FAILED',
    'Verification failed',
    'The username or verification code is not correct',
  );
}
