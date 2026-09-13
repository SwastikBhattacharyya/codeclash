import { HttpErrorResponse } from '@angular/common/http';

const NOT_FOUND_STATUSES = new Set([400, 404, 422]);

export function isNotFound(error: unknown): boolean {
  return error instanceof HttpErrorResponse && NOT_FOUND_STATUSES.has(error.status);
}
