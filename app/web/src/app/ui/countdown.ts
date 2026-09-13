import { Component, computed, inject, input } from '@angular/core';
import { Clock, describeDuration, formatDuration } from '../core/time/clock';

@Component({
  selector: 'cc-countdown',
  host: {
    role: 'timer',
    class: 'tabular-nums',
    '[attr.aria-label]': 'spoken()',
  },
  template: `{{ display() }}`,
})
export class Countdown {
  private readonly clock = inject(Clock);

  readonly until = input.required<string>();
  readonly label = input.required<string>();

  protected readonly remaining = computed(() => Date.parse(this.until()) - this.clock.now());
  protected readonly display = computed(() => formatDuration(this.remaining()));
  protected readonly spoken = computed(
    () => `${this.label()} ${describeDuration(this.remaining())}`,
  );
}
