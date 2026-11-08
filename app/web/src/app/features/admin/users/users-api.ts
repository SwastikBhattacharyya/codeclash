import { HttpClient, type HttpResourceRequest } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { USERS_URL, type User, userUrl } from '../../../core/account/user';
import { LOOKUP_PAGE_SIZE } from '../../../core/api/page';
import type { SortDirection } from '../../../ui/data-table';
import { ROLES_URL } from '../roles/roles-api';

export type AdminUser = User;

export type Role = {
  id: string;
  name: string;
};

export type UserSearchField = 'username' | 'name' | 'email';

export type UserSortField = 'name' | 'username' | 'email' | 'verified' | 'created_at';

export type UserQuery = {
  text: string;
  field: UserSearchField;
  roleId: string | null;
  verified: boolean | null;
  page: number;
  sort: UserSortField;
  direction: SortDirection;
};

export type UserCreate = {
  name: string;
  username: string;
  email: string;
  password: string;
  roleId?: string;
  verified?: boolean;
};

export type UserChanges = {
  name?: string;
  username?: string;
  email?: string;
  password?: string;
  roleId?: string;
  verified?: boolean;
};

export const USER_FORBIDDEN_MESSAGE = "You don't have permission to change this user.";
export const USER_ADMIN_PAGE_SIZE = 20;
export const USER_SEARCH_FIELDS: readonly UserSearchField[] = ['username', 'name', 'email'];
export const USER_SORT_FIELDS: readonly UserSortField[] = [
  'name',
  'username',
  'email',
  'verified',
  'created_at',
];

export function usersRequest(query: UserQuery): HttpResourceRequest {
  const params: Record<string, string> = {
    page: String(query.page),
    size: String(USER_ADMIN_PAGE_SIZE),
    sort: query.sort,
    direction: query.direction,
  };
  if (query.text !== '') {
    params[query.field] = query.text;
  }
  if (query.roleId !== null) {
    params['role_id'] = query.roleId;
  }
  if (query.verified !== null) {
    params['verified'] = String(query.verified);
  }
  return { url: USERS_URL, params };
}

export function rolesRequest(): HttpResourceRequest {
  return {
    url: ROLES_URL,
    params: { sort: 'name', direction: 'asc', size: String(LOOKUP_PAGE_SIZE) },
  };
}

@Service()
export class UsersApi {
  private readonly http = inject(HttpClient);

  create(body: UserCreate): Promise<AdminUser> {
    return firstValueFrom(this.http.post<AdminUser>(USERS_URL, body));
  }

  update(id: string, changes: UserChanges): Promise<AdminUser> {
    return firstValueFrom(this.http.patch<AdminUser>(userUrl(id), changes));
  }

  remove(id: string): Promise<void> {
    return firstValueFrom(this.http.delete<void>(userUrl(id)));
  }
}
