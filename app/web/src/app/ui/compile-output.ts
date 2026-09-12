import { Component, input } from '@angular/core';

@Component({
  selector: 'cc-compile-output',
  host: { class: 'block' },
  template: `
    <details class="rounded-control border border-line" [open]="open()">
      <summary class="cursor-pointer px-3 py-2 text-sm font-medium text-ink">
        Compiler output
      </summary>
      <div class="border-t border-line p-3">
        @if (output()) {
          <pre
            tabindex="0"
            role="region"
            aria-label="Compiler output"
            class="max-h-80 overflow-auto rounded-control bg-surface p-2 font-mono text-xs text-ink"
            >{{ output() }}</pre>
        } @else {
          <p class="text-sm text-ink-muted">The compiler produced no output.</p>
        }
      </div>
    </details>
  `,
})
export class CompileOutput {
  readonly output = input.required<string>();
  readonly open = input(true);
}
