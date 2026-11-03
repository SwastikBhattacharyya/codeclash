import { Component, input } from '@angular/core';
import type { AdminSection } from '../../core/admin/admin-sections';

@Component({
  selector: 'cc-admin-heading',
  host: { class: 'flex flex-col gap-2' },
  template: `
    <h1 class="text-3xl text-ink sm:text-4xl">{{ section().label }}</h1>
    <p class="max-w-2xl text-ink-muted">{{ section().description }}</p>
  `,
})
export class AdminHeading {
  readonly section = input.required<AdminSection>();
}
