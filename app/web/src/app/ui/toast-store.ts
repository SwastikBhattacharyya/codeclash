import { isPlatformBrowser } from '@angular/common';
import { inject, PLATFORM_ID, Service, signal } from '@angular/core';

export type ToastTone = 'success' | 'error' | 'info';

export type Toast = {
  id: number;
  message: string;
  tone: ToastTone;
};

export const TOAST_DURATION_MS = 6000;

type ToastTimer = {
  handle: ReturnType<typeof setTimeout> | null;
  remaining: number;
  startedAt: number;
};

@Service()
export class ToastStore {
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  private readonly state = signal<readonly Toast[]>([]);
  private readonly timers = new Map<number, ToastTimer>();
  private nextId = 0;

  readonly toasts = this.state.asReadonly();

  show(message: string, tone: ToastTone = 'success'): void {
    this.nextId += 1;
    const id = this.nextId;
    this.state.update((toasts) => [...toasts, { id, message, tone }]);
    if (this.isBrowser && tone !== 'error') {
      this.timers.set(id, { handle: null, remaining: TOAST_DURATION_MS, startedAt: 0 });
      this.resume(id);
    }
  }

  pause(id: number): void {
    const timer = this.timers.get(id);
    if (!timer || timer.handle === null) {
      return;
    }
    clearTimeout(timer.handle);
    timer.handle = null;
    timer.remaining -= Date.now() - timer.startedAt;
  }

  resume(id: number): void {
    const timer = this.timers.get(id);
    if (!timer || timer.handle !== null) {
      return;
    }
    timer.startedAt = Date.now();
    timer.handle = setTimeout(() => this.dismiss(id), Math.max(timer.remaining, 0));
  }

  dismiss(id: number): void {
    const timer = this.timers.get(id);
    if (timer?.handle) {
      clearTimeout(timer.handle);
    }
    this.timers.delete(id);
    this.state.update((toasts) => toasts.filter((toast) => toast.id !== id));
  }
}
