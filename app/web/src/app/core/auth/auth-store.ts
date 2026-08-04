import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { computed, inject, Service, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { SESSION_URL, type Session } from './session';

@Service()
export class AuthStore {
  private readonly http = inject(HttpClient);
  private readonly state = signal<Session | null>(null);

  readonly session = this.state.asReadonly();
  readonly isAuthenticated = computed(() => this.state() !== null);

  async load(): Promise<void> {
    try {
      this.state.set(await firstValueFrom(this.http.get<Session>(SESSION_URL)));
    } catch {
      this.state.set(null);
    }
  }

  async refresh(): Promise<void> {
    try {
      this.state.set(await firstValueFrom(this.http.get<Session>(SESSION_URL)));
    } catch (error) {
      if (error instanceof HttpErrorResponse && error.status === 401) {
        this.state.set(null);
      }
    }
  }

  async signIn(login: string, password: string, rememberMe: boolean): Promise<Session> {
    const session = await firstValueFrom(
      this.http.post<Session>(SESSION_URL, { login, password, rememberMe }),
    );
    this.state.set(session);
    return session;
  }

  async signOut(): Promise<void> {
    await firstValueFrom(this.http.delete<void>(SESSION_URL));
    this.state.set(null);
  }

  clear(): void {
    this.state.set(null);
  }
}
