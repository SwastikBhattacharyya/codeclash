import { DOCUMENT, isPlatformBrowser } from '@angular/common';
import { inject, PLATFORM_ID, REQUEST, Service, signal } from '@angular/core';

export type ThemePreference = 'system' | 'light' | 'dark';

export const THEME_COOKIE = 'cc_theme';
export const THEME_COOKIE_MAX_AGE_SECONDS = 365 * 24 * 60 * 60;
export const THEME_ATTRIBUTE = 'data-theme';

@Service()
export class ThemeStore {
  private readonly document = inject(DOCUMENT);
  private readonly request = inject(REQUEST);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  private readonly state = signal<ThemePreference>(this.readPreference());

  readonly preference = this.state.asReadonly();

  init(): void {
    this.apply(this.state());
  }

  set(preference: ThemePreference): void {
    this.state.set(preference);
    this.writeCookie(preference);
    this.apply(preference);
  }

  private readPreference(): ThemePreference {
    const header = this.isBrowser
      ? this.document.cookie
      : (this.request?.headers.get('cookie') ?? null);
    for (const part of (header ?? '').split(';')) {
      const [name, value] = part.trim().split('=');
      if (name === THEME_COOKIE && (value === 'light' || value === 'dark')) {
        return value;
      }
    }
    return 'system';
  }

  private apply(preference: ThemePreference): void {
    const root = this.document.documentElement;
    if (preference === 'system') {
      root.removeAttribute(THEME_ATTRIBUTE);
    } else {
      root.setAttribute(THEME_ATTRIBUTE, preference);
    }
  }

  private writeCookie(preference: ThemePreference): void {
    if (!this.isBrowser) {
      return;
    }
    const secure = this.document.location.protocol === 'https:' ? '; Secure' : '';
    this.document.cookie =
      preference === 'system'
        ? `${THEME_COOKIE}=; Max-Age=0; Path=/; SameSite=Lax${secure}`
        : `${THEME_COOKIE}=${preference}; Max-Age=${THEME_COOKIE_MAX_AGE_SECONDS}; Path=/; SameSite=Lax${secure}`;
  }
}
