import { computed, inject, Service } from '@angular/core';
import { PERMISSION_READ, ROLE_WRITE, USER_READ_ROLE } from '../../../core/admin/admin-access';
import { AuthStore } from '../../../core/auth/auth-store';
import type { AdminRole } from './roles-api';

@Service()
export class RoleAccess {
  private readonly auth = inject(AuthStore);
  private readonly permissions = computed(() => new Set(this.auth.session()?.permissions ?? []));

  readonly canWrite = computed(() => this.permissions().has(ROLE_WRITE));
  readonly canReadPermissions = computed(() => this.permissions().has(PERMISSION_READ));
  readonly canCountMembers = computed(() => this.permissions().has(USER_READ_ROLE));

  canEdit(role: AdminRole): boolean {
    return this.canWrite() && !role.isDefault;
  }

  isOwn(role: AdminRole): boolean {
    return this.auth.session()?.roleName === role.name;
  }
}
