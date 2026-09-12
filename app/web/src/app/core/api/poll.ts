import { isPlatformBrowser } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { DOCUMENT, effect, inject, PLATFORM_ID, untracked } from '@angular/core';

export type Reloadable = {
  isLoading(): boolean;
  error(): unknown;
  reload(): boolean;
};

const FINAL_ERROR_STATUSES: ReadonlySet<number> = new Set([401, 403, 404]);
const MAX_BACKOFF_MS = 30_000;

export function isFinalPollError(error: unknown): boolean {
  return error instanceof HttpErrorResponse && FINAL_ERROR_STATUSES.has(error.status);
}

export function pollWhile(
  resource: Reloadable,
  active: () => boolean,
  interval: () => number,
): void {
  if (!isPlatformBrowser(inject(PLATFORM_ID))) {
    return;
  }
  const document = inject(DOCUMENT);
  let failures = 0;
  effect((onCleanup) => {
    if (!active() || resource.isLoading()) {
      return;
    }
    const error = resource.error();
    if (isFinalPollError(error)) {
      return;
    }
    failures = error ? failures + 1 : 0;
    const base = untracked(interval);
    const delay = failures === 0 ? base : Math.min(MAX_BACKOFF_MS, base * 2 ** failures);
    let listening = false;
    const stopListening = (): void => {
      if (listening) {
        document.removeEventListener('visibilitychange', resume);
        listening = false;
      }
    };
    const resume = (): void => {
      if (document.visibilityState === 'visible') {
        stopListening();
        resource.reload();
      }
    };
    const handle = setTimeout(() => {
      if (document.visibilityState === 'visible') {
        resource.reload();
        return;
      }
      listening = true;
      document.addEventListener('visibilitychange', resume);
    }, delay);
    onCleanup(() => {
      clearTimeout(handle);
      stopListening();
    });
  });
}
