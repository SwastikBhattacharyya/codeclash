import { HttpErrorResponse } from '@angular/common/http';

export function isGone(error: unknown): boolean {
  return error instanceof HttpErrorResponse && error.status === 404;
}
