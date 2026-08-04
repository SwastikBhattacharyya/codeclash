export type Session = {
  userId: string;
  roleName: string | null;
  permissions: string[];
  lastUsedAt: string;
  createdAt: string;
  updatedAt: string;
};

export const SESSION_URL = '/bff/session';
