import { Component, computed, input } from '@angular/core';
import { STATE_LABELS, type SubmissionState, verdictTone } from '../core/submissions/submissions';

@Component({
  selector: 'cc-verdict-badge',
  host: { class: 'inline-flex' },
  template: `
    <span
      class="rounded-chip px-2 py-0.5 text-xs font-medium whitespace-nowrap"
      [class.bg-success/10]="tone() === 'success'"
      [class.text-success]="tone() === 'success'"
      [class.bg-danger/10]="tone() === 'danger'"
      [class.text-danger]="tone() === 'danger'"
      [class.bg-accent/25]="tone() === 'pending'"
      [class.text-ink]="tone() === 'pending' || tone() === 'neutral'"
      [class.bg-ink/6]="tone() === 'neutral'"
      >{{ label() }}</span
    >
  `,
})
export class VerdictBadge {
  readonly state = input.required<SubmissionState>();

  protected readonly tone = computed(() => verdictTone(this.state()));
  protected readonly label = computed(() => STATE_LABELS[this.state()]);
}
