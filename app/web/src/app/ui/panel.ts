import { Component } from '@angular/core';
import { cn } from './cn';

@Component({
  selector: 'cc-panel',
  host: { '[class]': 'classes' },
  template: `<ng-content />`,
})
export class Panel {
  protected readonly classes = cn('block rounded-panel border border-line bg-raised p-6 sm:p-8');
}
