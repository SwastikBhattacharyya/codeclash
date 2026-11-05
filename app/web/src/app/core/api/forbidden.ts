import { HttpErrorResponse } from '@angular/common/http';
import { toApiError } from './api-errors';

export function missingPermissions(error: unknown): string[] {
  if (!(error instanceof HttpErrorResponse) || error.status !== 403) {
    return [];
  }
  const permissions = toApiError(error)?.properties?.['permissions'];
  return Array.isArray(permissions)
    ? permissions.filter((permission): permission is string => typeof permission === 'string')
    : [];
}
