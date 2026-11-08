import type { CanDeactivateFn } from '@angular/router';
import type { ContestDetail } from './contest-detail';

export const contestUnsavedGuard: CanDeactivateFn<ContestDetail> = (component) =>
  component.canLeave();
