export type User = {
  id: string;
  name: string;
  username: string;
  email: string | null;
  isVerified: boolean;
  roleId: string | null;
  createdAt: string;
  updatedAt: string;
};

export type SignUpRequest = {
  name: string;
  username: string;
  email: string;
  password: string;
};

export type ProfileChanges = {
  name?: string;
  username?: string;
};

export type PasswordChangeResult = {
  otherSessionsSignedOut: boolean;
};

export const USERS_URL = '/api/v1/users';
export const VERIFY_EMAIL_URL = '/bff/verify-email';
export const PASSWORD_URL = '/bff/password';

export function userUrl(id: string): string {
  return `${USERS_URL}/${encodeURIComponent(id)}`;
}
