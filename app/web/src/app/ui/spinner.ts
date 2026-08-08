import { Component } from '@angular/core';
import { LucideLoaderCircle } from '@lucide/angular';

@Component({
  selector: 'cc-spinner',
  imports: [LucideLoaderCircle],
  host: { class: 'inline-flex' },
  template: `<svg lucideLoaderCircle class="cc-spin size-full animate-spin"></svg>`,
})
export class Spinner {}
