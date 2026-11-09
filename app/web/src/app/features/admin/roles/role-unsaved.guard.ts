import type { CanDeactivateFn } from '@angular/router';
import type { RoleDetail } from './role-detail';

export const roleUnsavedGuard: CanDeactivateFn<RoleDetail> = (component) => component.canLeave();
