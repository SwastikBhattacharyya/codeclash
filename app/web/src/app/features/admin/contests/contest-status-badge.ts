import { Component, computed, inject, input } from '@angular/core';
import type { ContestSummary } from '../../../core/contests/contests-api';
import { Clock } from '../../../core/time/clock';
import { cn } from '../../../ui/cn';
import { CONTEST_STATUS_LABELS, type ContestStatus, contestStatus } from './contests-api';

const STATUS_CLASSES: Readonly<Record<ContestStatus, string>> = {
  upcoming: cn('border border-line text-ink'),
  running: cn('bg-success/10 text-success'),
  ended: cn('bg-ink/6 text-ink-muted'),
};

@Component({
  selector: 'cc-contest-status-badge',
  host: { class: 'inline-flex' },
  template: `<span [class]="classes()">{{ label() }}</span>`,
})
export class ContestStatusBadge {
  private readonly clock = inject(Clock);

  readonly contest = input.required<ContestSummary>();
  readonly large = input(false);

  private readonly status = computed(() => contestStatus(this.contest(), this.clock.now()));
  protected readonly label = computed(() => CONTEST_STATUS_LABELS[this.status()]);
  protected readonly classes = computed(() =>
    cn(
      'rounded-chip px-2 py-0.5 font-medium whitespace-nowrap',
      this.large() ? 'text-sm' : 'text-xs',
      STATUS_CLASSES[this.status()],
    ),
  );
}
