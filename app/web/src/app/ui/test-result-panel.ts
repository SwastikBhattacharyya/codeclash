import { Component, computed, input } from '@angular/core';
import { firstDifferenceLine, type SubmissionState } from '../core/submissions/submissions';

@Component({
  selector: 'cc-test-result-panel',
  host: { class: '@container block' },
  template: `
    <div class="grid gap-3" [class]="columns()">
      <div class="min-w-0">
        <p class="mb-1 text-xs font-medium text-ink-muted">Input</p>
        <pre
          tabindex="0"
          role="region"
          aria-label="Input"
          class="max-h-60 overflow-auto rounded-control bg-surface p-2 font-mono text-xs text-ink"
          >{{ input() }}</pre>
      </div>
      <div class="min-w-0">
        <p class="mb-1 text-xs font-medium text-ink-muted">Expected output</p>
        <pre
          tabindex="0"
          role="region"
          aria-label="Expected output"
          class="max-h-60 overflow-auto rounded-control bg-surface p-2 font-mono text-xs text-ink"
          >{{ expected() }}</pre>
      </div>
      @if (actual(); as output) {
        <div class="min-w-0">
          <p class="mb-1 text-xs font-medium text-ink-muted">{{ actualLabel() }}</p>
          <pre
            tabindex="0"
            role="region"
            [attr.aria-label]="actualLabel()"
            class="max-h-60 overflow-auto rounded-control bg-surface p-2 font-mono text-xs text-ink"
            >{{ output }}</pre>
        </div>
      } @else if (actual() === '') {
        <div class="min-w-0">
          <p class="mb-1 text-xs font-medium text-ink-muted">{{ actualLabel() }}</p>
          <p class="rounded-control bg-surface p-2 text-xs text-ink-muted">
            Your program printed nothing.
          </p>
        </div>
      }
    </div>
    @if (difference(); as line) {
      <p class="mt-2 text-sm text-ink-muted">First difference at line {{ line }}.</p>
    }
  `,
})
export class TestResultPanel {
  readonly input = input.required<string>();
  readonly expected = input.required<string>();
  readonly actual = input<string | null>(null);
  readonly state = input.required<SubmissionState>();

  protected readonly columns = computed(() =>
    this.actual() === null ? '@2xl:grid-cols-2' : '@2xl:grid-cols-3',
  );
  protected readonly actualLabel = computed(() =>
    this.state() === 'runtime_error' ? 'Output before the crash' : 'Your output',
  );
  protected readonly difference = computed(() => {
    const actual = this.actual();
    if (this.state() !== 'incorrect' || actual === null) {
      return null;
    }
    return firstDifferenceLine(this.expected(), actual);
  });
}
