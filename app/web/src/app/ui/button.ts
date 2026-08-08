import { booleanAttribute, Component, computed, ElementRef, inject, input } from '@angular/core';
import { cn } from './cn';
import { Spinner } from './spinner';

export type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger';

const BASE_CLASSES = cn(
  'inline-flex min-h-11 cursor-pointer items-center justify-center gap-2 rounded-control border px-5 text-base font-semibold transition-colors select-none disabled:cursor-not-allowed disabled:opacity-60 aria-disabled:cursor-not-allowed aria-disabled:opacity-60',
);

const VARIANT_CLASSES: Record<ButtonVariant, string> = {
  primary: cn('border-ink/15 bg-accent text-on-accent hover:bg-accent/85'),
  secondary: cn('border-control bg-raised text-ink hover:border-ink-muted'),
  ghost: cn('border-transparent bg-transparent text-ink hover:bg-ink/6'),
  danger: cn('border-transparent bg-danger text-raised hover:bg-danger/90'),
};

@Component({
  selector: 'button[ccButton], a[ccButton]',
  imports: [Spinner],
  host: {
    '[class]': 'classes()',
    '[attr.aria-busy]': 'loading() ? "true" : null',
    '[attr.disabled]': 'isButton && disabled() ? "" : null',
    '[attr.aria-disabled]': 'blocked() && !(isButton && disabled()) ? "true" : null',
    '[attr.tabindex]': '!isButton && disabled() ? "-1" : null',
    '(click)': 'onClick($event)',
  },
  template: `
    @if (loading()) {
      <cc-spinner class="size-5" />
    }
    <ng-content />
  `,
})
export class Button {
  readonly variant = input<ButtonVariant>('primary');
  readonly loading = input(false, { transform: booleanAttribute });
  readonly disabled = input(false, { transform: booleanAttribute });

  protected readonly isButton =
    inject<ElementRef<HTMLElement>>(ElementRef).nativeElement.tagName === 'BUTTON';
  protected readonly blocked = computed(() => this.disabled() || this.loading());
  protected readonly classes = computed(() => cn(BASE_CLASSES, VARIANT_CLASSES[this.variant()]));

  protected onClick(event: Event): void {
    if (this.blocked()) {
      event.preventDefault();
      event.stopImmediatePropagation();
    }
  }
}
