import { Component, computed, input } from '@angular/core';
import { LucideCircleCheck, LucideCircleExclamationPoint, LucideInfo } from '@lucide/angular';
import { cn } from './cn';

export type AlertTone = 'error' | 'success' | 'info';

const TONE_CLASSES: Record<AlertTone, string> = {
  error: cn('border-danger/40 bg-danger/8'),
  success: cn('border-success/40 bg-success/8'),
  info: cn('border-line bg-raised'),
};

@Component({
  selector: 'cc-alert',
  imports: [LucideCircleCheck, LucideCircleExclamationPoint, LucideInfo],
  host: { '[class]': 'classes()', '[attr.role]': 'role()' },
  templateUrl: './alert.html',
})
export class Alert {
  readonly tone = input<AlertTone>('info');

  protected readonly role = computed(() => (this.tone() === 'error' ? 'alert' : 'status'));
  protected readonly classes = computed(() =>
    cn('flex gap-3 rounded-control border px-4 py-3 text-sm text-ink', TONE_CLASSES[this.tone()]),
  );
}
