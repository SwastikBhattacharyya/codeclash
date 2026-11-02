import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideChevronRight } from '@lucide/angular';
import { allowedGroups } from '../../../core/admin/admin-sections';
import { AuthStore } from '../../../core/auth/auth-store';

import { AdminSectionIcon } from '../../../ui/admin-section-icon';

@Component({
  selector: 'cc-admin-overview',
  imports: [AdminSectionIcon, RouterLink, LucideChevronRight],
  host: { class: 'flex flex-col gap-8 sm:gap-10' },
  templateUrl: './overview.html',
})
export class AdminOverview {
  private readonly auth = inject(AuthStore);

  protected readonly groups = computed(() => allowedGroups(this.auth.session()?.permissions ?? []));
}
