import { afterNextRender, type Injector } from '@angular/core';
import type { FieldTree, ValidationError } from '@angular/forms/signals';
import { errorMessage, toApiError } from '../api/api-errors';

export type ApiField =
  | 'name'
  | 'username'
  | 'email'
  | 'password'
  | 'currentPassword'
  | 'description'
  | 'roleId'
  | 'title'
  | 'summary'
  | 'maxRunTimeMs'
  | 'maxRamLimitMb'
  | 'input'
  | 'output'
  | 'liveAt'
  | 'endAt';

export type SubmitFailure = {
  fieldErrors: ValidationError.WithOptionalFieldTree[];
  formError: string | null;
};

const FIELD_PREFIXES: readonly [string, ApiField][] = [
  ['CURRENT_PASSWORD', 'currentPassword'],
  ['PASSWORD', 'password'],
  ['USERNAME', 'username'],
  ['EMAIL', 'email'],
  ['NAME', 'name'],
  ['CATEGORY_NAME', 'name'],
  ['TAG_NAME', 'name'],
  ['DESCRIPTION', 'description'],
  ['ROLE_NAME', 'name'],
  ['ROLE', 'roleId'],
  ['ROLE_ID', 'roleId'],
  ['ROLE_NOT_FOUND', 'roleId'],
  ['TITLE', 'title'],
  ['SUMMARY', 'summary'],
  ['MAX_RUN_TIME', 'maxRunTimeMs'],
  ['MAX_RAM_LIMIT', 'maxRamLimitMb'],
  ['INPUT', 'input'],
  ['OUTPUT', 'output'],
  ['CONTEST_NAME', 'name'],
  ['LIVE_AT', 'liveAt'],
  ['LIVE_AT_IN_PAST', 'liveAt'],
  ['END_AT', 'endAt'],
  ['END_AT_IN_PAST', 'endAt'],
  ['END_AT_NOT_AFTER_LIVE_AT', 'endAt'],
];

export function fieldForCode(code: string): ApiField | null {
  const normalized = code
    .replace(/^(NULL|INVALID|DUPLICATE)_/, '')
    .replace(/_(TOO_SHORT|TOO_LONG|TOO_LOW|TOO_HIGH|MISSING_[A-Z_]+|INCORRECT)$/, '');
  for (const [prefix, field] of FIELD_PREFIXES) {
    if (normalized === prefix) {
      return field;
    }
  }
  return null;
}

export function toSubmitFailure(
  error: unknown,
  fields: Partial<Record<ApiField, FieldTree<unknown>>>,
  overrides: Readonly<Record<string, string>> = {},
): SubmitFailure {
  const apiError = toApiError(error);
  const message = apiError
    ? (overrides[apiError.code] ?? errorMessage(error))
    : errorMessage(error);
  const field = apiError ? fieldForCode(apiError.code) : null;
  const fieldTree = field ? fields[field] : undefined;
  return fieldTree
    ? { fieldErrors: [{ kind: 'server', message, fieldTree }], formError: null }
    : { fieldErrors: [], formError: message };
}

export function focusFirstInvalid(fields: readonly FieldTree<unknown>[]): void {
  fields
    .find((field) => field().invalid())?.()
    .focusBoundControl();
}

export function focusFirstInvalidAfterRender(
  injector: Injector,
  fields: readonly FieldTree<unknown>[],
): void {
  afterNextRender(() => focusFirstInvalid(fields), { injector });
}
