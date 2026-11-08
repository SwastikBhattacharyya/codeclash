import { HttpClient, type HttpResourceRequest } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { USERS_URL } from '../../../core/account/user';
import { LOOKUP_PAGE_SIZE } from '../../../core/api/page';
import type { SortDirection } from '../../../ui/data-table';

export type AdminRole = {
  id: string;
  name: string;
  description: string;
  isDefault: boolean;
  permissions: string[];
  createdAt: string;
  updatedAt: string;
};

export type Permission = {
  id: string;
  name: string;
  description: string;
};

export type RoleType = 'default' | 'custom';

export type RoleSortField = 'name' | 'is_default' | 'created_at';

export type RoleQuery = {
  name: string;
  type: RoleType | null;
  page: number;
  sort: RoleSortField;
  direction: SortDirection;
};

export type RoleInput = {
  name: string;
  description: string;
  permissions: string[];
};

export type RoleChanges = Partial<RoleInput>;

export const ROLES_URL = '/api/v1/roles';
export const PERMISSIONS_URL = '/api/v1/permissions';
export const ROLE_ADMIN_PAGE_SIZE = 20;
export const ROLE_FORBIDDEN_MESSAGE = "You don't have permission to change roles.";
export const ROLE_TYPES: readonly RoleType[] = ['default', 'custom'];
export const ROLE_SORT_FIELDS: readonly RoleSortField[] = ['name', 'is_default', 'created_at'];

export function roleUrl(id: string): string {
  return `${ROLES_URL}/${encodeURIComponent(id)}`;
}

export function rolesListRequest(query: RoleQuery): HttpResourceRequest {
  const params: Record<string, string> = {
    page: String(query.page),
    size: String(ROLE_ADMIN_PAGE_SIZE),
    sort: query.sort,
    direction: query.direction,
  };
  if (query.name !== '') {
    params['name'] = query.name;
  }
  if (query.type !== null) {
    params['default'] = String(query.type === 'default');
  }
  return { url: ROLES_URL, params };
}

export function permissionsRequest(): HttpResourceRequest {
  return {
    url: PERMISSIONS_URL,
    params: { sort: 'name', direction: 'asc', size: String(LOOKUP_PAGE_SIZE) },
  };
}

export function roleMembersRequest(id: string): HttpResourceRequest {
  return { url: USERS_URL, params: { role_id: id, size: '1' } };
}

@Service()
export class RolesApi {
  private readonly http = inject(HttpClient);

  create(input: RoleInput): Promise<AdminRole> {
    return firstValueFrom(this.http.post<AdminRole>(ROLES_URL, input));
  }

  update(id: string, changes: RoleChanges): Promise<AdminRole> {
    return firstValueFrom(this.http.patch<AdminRole>(roleUrl(id), changes));
  }

  remove(id: string): Promise<void> {
    return firstValueFrom(this.http.delete<void>(roleUrl(id)));
  }
}
