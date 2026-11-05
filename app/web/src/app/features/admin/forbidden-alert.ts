import { Component, input } from '@angular/core';
import { Alert } from '../../ui/alert';

@Component({
  selector: 'cc-forbidden-alert',
  imports: [Alert],
  host: { class: 'block' },
  template: `
    <cc-alert tone="error">
      <p>{{ message() }}</p>
      @if (permissions().length > 0) {
        <ul class="mt-1.5 flex flex-col gap-0.5 font-mono text-xs break-all">
          @for (permission of permissions(); track permission) {
            <li>{{ permission }}</li>
          }
        </ul>
      }
    </cc-alert>
  `,
})
export class ForbiddenAlert {
  readonly message = input.required<string>();
  readonly permissions = input<readonly string[]>([]);
}
