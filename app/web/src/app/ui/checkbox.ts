import { Component, type ElementRef, input, model, output, viewChild } from '@angular/core';
import type { FormCheckboxControl } from '@angular/forms/signals';
import { LucideCheck } from '@lucide/angular';

@Component({
  selector: 'cc-checkbox',
  imports: [LucideCheck],
  host: { class: 'block' },
  templateUrl: './checkbox.html',
})
export class Checkbox implements FormCheckboxControl {
  readonly checked = model(false);
  readonly label = input.required<string>();
  readonly disabled = input(false);
  readonly name = input('');
  readonly touch = output<void>();

  private readonly control = viewChild.required<ElementRef<HTMLInputElement>>('control');

  focus(options?: FocusOptions): void {
    this.control().nativeElement.focus(options);
  }

  protected onChange(event: Event): void {
    this.checked.set((event.target as HTMLInputElement).checked);
  }
}
