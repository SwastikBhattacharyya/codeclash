import { computed, inject, Service } from '@angular/core';
import {
  ROLE_READ,
  SUBMISSION_DELETE,
  USER_READ_EMAIL,
  USER_READ_ROLE,
  USER_WRITE,
  USER_WRITE_IS_VERIFIED,
  USER_WRITE_IS_VERIFIED_OWN,
  USER_WRITE_OWN,
  USER_WRITE_ROLE,
  USER_WRITE_ROLE_OWN,
  USER_WRITE_USERNAME,
  USER_WRITE_USERNAME_OWN,
} from '../../../core/admin/admin-access';
import { AuthStore } from '../../../core/auth/auth-store';
import type { AdminUser } from './users-api';

@Service()
export class UserAccess {
  private readonly auth = inject(AuthStore);
  private readonly permissions = computed(() => new Set(this.auth.session()?.permissions ?? []));

  readonly userId = computed(() => this.auth.session()?.userId ?? null);
  readonly canReadEmail = computed(() => this.has(USER_READ_EMAIL));
  readonly canReadRoles = computed(() => this.has(USER_READ_ROLE) && this.has(ROLE_READ));
  readonly canViewSubmissions = computed(() => this.has(SUBMISSION_DELETE));

  isSelf(user: AdminUser | null): boolean {
    return user !== null && user.id === this.userId();
  }

  canWrite(user: AdminUser | null): boolean {
    return this.hasFor(user, USER_WRITE, USER_WRITE_OWN);
  }

  canDelete(user: AdminUser): boolean {
    return !this.isSelf(user) && this.has(USER_WRITE);
  }

  canWriteUsername(user: AdminUser | null): boolean {
    return this.hasFor(user, USER_WRITE_USERNAME, USER_WRITE_USERNAME_OWN);
  }

  canWriteRole(user: AdminUser | null): boolean {
    return this.hasFor(user, USER_WRITE_ROLE, USER_WRITE_ROLE_OWN);
  }

  canWriteVerified(user: AdminUser | null): boolean {
    return this.hasFor(user, USER_WRITE_IS_VERIFIED, USER_WRITE_IS_VERIFIED_OWN);
  }

  private hasFor(user: AdminUser | null, permission: string, ownPermission: string): boolean {
    return this.has(this.isSelf(user) ? ownPermission : permission);
  }

  private has(permission: string): boolean {
    return this.permissions().has(permission);
  }
}
