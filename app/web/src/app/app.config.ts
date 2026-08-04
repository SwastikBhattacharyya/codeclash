import { provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  type ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideClientHydration } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { AuthStore } from './core/auth/auth-store';
import { unauthorizedInterceptor } from './core/auth/unauthorized.interceptor';
import { ThemeStore } from './core/theme/theme-store';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideClientHydration(),
    provideHttpClient(withInterceptors([unauthorizedInterceptor])),
    provideAppInitializer(() => inject(ThemeStore).init()),
    provideAppInitializer(() => inject(AuthStore).load()),
  ],
};
