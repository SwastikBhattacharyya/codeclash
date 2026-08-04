import { HttpBackend } from '@angular/common/http';
import { type ApplicationConfig, mergeApplicationConfig } from '@angular/core';
import { provideServerRendering, withRoutes } from '@angular/ssr';
import { appConfig } from './app.config';
import { serverRoutes } from './app.routes.server';
import { ServerApiBackend } from './core/auth/server-api.backend';

const serverConfig: ApplicationConfig = {
  providers: [
    provideServerRendering(withRoutes(serverRoutes)),
    { provide: HttpBackend, useClass: ServerApiBackend },
  ],
};

export const config = mergeApplicationConfig(appConfig, serverConfig);
