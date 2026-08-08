import { HttpErrorResponse } from '@angular/common/http';
import type { ApiError } from './api-error';

export const GENERIC_ERROR_MESSAGE = 'Something went wrong. Check your connection and try again.';

export function toApiError(error: unknown): ApiError | null {
  if (!(error instanceof HttpErrorResponse)) {
    return null;
  }
  const body = error.error as Partial<ApiError> | null;
  if (body && typeof body.code === 'string' && typeof body.message === 'string') {
    return body as ApiError;
  }
  return null;
}

export function errorMessage(error: unknown): string {
  const apiError = toApiError(error);
  if (!apiError || (error instanceof HttpErrorResponse && error.status >= 500)) {
    return GENERIC_ERROR_MESSAGE;
  }
  return apiError.message;
}
