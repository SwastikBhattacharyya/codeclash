import { join } from 'node:path';
import {
  AngularNodeAppEngine,
  createNodeRequestHandler,
  isMainModule,
  writeResponseToNodeResponse,
} from '@angular/ssr/node';
import express from 'express';
import { rejectCrossSite } from './server/csrf';
import { apiUrl } from './server/env';
import { handleError, noStore } from './server/errors';
import { passwordRouter } from './server/password-routes';
import { proxyToGateway } from './server/proxy';
import { sessionRouter } from './server/session-routes';
import { verifyRouter } from './server/verify-routes';

const browserDistFolder = join(import.meta.dirname, '../browser');

const app = express();
const angularApp = new AngularNodeAppEngine();

app.set('trust proxy', trustProxy(process.env['TRUST_PROXY']));
app.disable('x-powered-by');

app.use(['/api', '/bff'], noStore, rejectCrossSite);
app.use('/bff/session', sessionRouter);
app.use('/bff/verify-email', verifyRouter);
app.use('/bff/password', passwordRouter);
app.use('/api', proxyToGateway);

app.use(
  express.static(browserDistFolder, {
    maxAge: '1y',
    index: false,
    redirect: false,
  }),
);

app.use((req, res, next) => {
  angularApp
    .handle(req)
    .then((response) => {
      if (!response) {
        next();
        return;
      }
      res.setHeader('cache-control', 'private, no-store');
      return writeResponseToNodeResponse(response, res);
    })
    .catch(next);
});

app.use(handleError);

if (isMainModule(import.meta.url) || process.env['pm_id']) {
  apiUrl();
  const port = process.env['PORT'] || 4000;
  const server = app.listen(port, (error) => {
    if (error) {
      throw error;
    }

    console.log(`Node Express server listening on http://localhost:${port}`);
  });
  for (const signal of ['SIGTERM', 'SIGINT'] as const) {
    process.once(signal, () => {
      server.close(() => process.exit(0));
      server.closeIdleConnections();
    });
  }
}

function trustProxy(value: string | undefined): boolean | number | string {
  if (!value) {
    return 'loopback';
  }
  if (value === 'true' || value === 'false') {
    return value === 'true';
  }
  if (/^\d+$/.test(value)) {
    return Number(value);
  }
  return value;
}

export const reqHandler = createNodeRequestHandler(app);
