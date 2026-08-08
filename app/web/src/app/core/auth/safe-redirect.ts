import { PROFILE_PATH } from './auth-paths';

const AUTH_PREFIX = '/auth';
const FIRST_PRINTABLE_CODE = 0x20;
const DELETE_CODE = 0x7f;

export function safeRedirect(value: string | null | undefined): string {
  if (
    !value?.startsWith('/') ||
    value.startsWith('//') ||
    hasUnsafeCharacter(value) ||
    value === AUTH_PREFIX ||
    value.startsWith(`${AUTH_PREFIX}/`) ||
    value.startsWith(`${AUTH_PREFIX}?`)
  ) {
    return PROFILE_PATH;
  }
  return value;
}

function hasUnsafeCharacter(value: string): boolean {
  for (const character of value) {
    const code = character.charCodeAt(0);
    if (character === '\\' || code < FIRST_PRINTABLE_CODE || code === DELETE_CODE) {
      return true;
    }
  }
  return false;
}
