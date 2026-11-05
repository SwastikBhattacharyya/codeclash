import type { CanDeactivateFn } from '@angular/router';
import type { QuestionDetail } from './question-detail';

export const questionUnsavedGuard: CanDeactivateFn<QuestionDetail> = (component) =>
  component.canLeave();
