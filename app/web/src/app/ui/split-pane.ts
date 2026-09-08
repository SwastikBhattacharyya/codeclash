import {
  Component,
  computed,
  type ElementRef,
  inject,
  input,
  linkedSignal,
  signal,
  viewChild,
} from '@angular/core';
import {LocalStore} from '../core/storage/local-store';

const MIN_RATIO = 25;
const MAX_RATIO = 75;
const KEY_STEP = 2;
const PERCENT = 100;

@Component({
  selector: 'cc-split-pane',
  host: {class: 'block min-h-0'},
  template: `
    <div
      #container
      class="flex h-full min-h-0 flex-col gap-8 lg:flex-row lg:gap-0 lg:pr-3"
      [style.--split]="ratio() + '%'"
    >
      <div
        class="relative min-w-0 lg:min-h-0 lg:flex-[0_0_var(--split)] lg:overflow-y-auto lg:pr-5"
      >
        <ng-content select="[ccPaneStart]"/>
      </div>
      <div
        role="separator"
        tabindex="0"
        aria-orientation="vertical"
        [attr.aria-label]="label()"
        [attr.aria-valuenow]="ratio()"
        [attr.aria-valuemin]="minRatio"
        [attr.aria-valuemax]="maxRatio"
        class="group hidden w-3 shrink-0 cursor-col-resize touch-none justify-center outline-none lg:flex"
        (pointerdown)="startDrag($event)"
        (pointermove)="drag($event)"
        (pointerup)="endDrag($event)"
        (pointercancel)="endDrag($event)"
        (lostpointercapture)="endDrag($event)"
        (keydown)="onKey($event)"
      >
        <span
          class="transition-colors group-hover:w-0.5 group-hover:bg-ink-muted group-focus-visible:w-0.5 group-focus-visible:bg-ink"
          [class]="dragging() ? 'w-0.5 bg-ink' : 'w-px bg-line'"
        ></span>
      </div>
      <div class="relative min-w-0 flex-1 lg:min-h-0 lg:overflow-y-auto lg:px-5">
        <ng-content select="[ccPaneEnd]"/>
      </div>
    </div>
  `,
})
export class SplitPane {
  readonly storageKey = input.required<string>();
  readonly initialRatio = input(45);
  readonly label = input('Resize panels');
  protected readonly minRatio = MIN_RATIO;
  protected readonly maxRatio = MAX_RATIO;
  protected readonly dragging = signal(false);
  private readonly store = inject(LocalStore);
  private readonly container = viewChild.required<ElementRef<HTMLElement>>('container');
  private bounds: DOMRect | null = null;

  private readonly stored = computed(() => {
    const saved = Number.parseFloat(this.store.get(this.storageKey()) ?? '');
    return Number.isFinite(saved) ? SplitPane.clamp(saved) : this.initialRatio();
  });
  protected readonly ratio = linkedSignal(() => this.stored());

  private static keyRatio(key: string, current: number, initial: number): number | null {
    switch (key) {
      case 'ArrowLeft':
        return SplitPane.clamp(current - KEY_STEP);
      case 'ArrowRight':
        return SplitPane.clamp(current + KEY_STEP);
      case 'Home':
        return MIN_RATIO;
      case 'End':
        return MAX_RATIO;
      case 'Enter':
        return initial;
      default:
        return null;
    }
  }

  private static clamp(value: number): number {
    return Math.min(MAX_RATIO, Math.max(MIN_RATIO, Math.round(value * 10) / 10));
  }

  protected startDrag(event: PointerEvent): void {
    if (event.button !== 0) {
      return;
    }
    this.dragging.set(true);
    this.bounds = this.container().nativeElement.getBoundingClientRect();
    (event.currentTarget as HTMLElement).setPointerCapture(event.pointerId);
    event.preventDefault();
  }

  protected drag(event: PointerEvent): void {
    const bounds = this.bounds;
    if (!this.dragging() || bounds === null) {
      return;
    }
    if (event.buttons === 0) {
      this.endDrag(event);
      return;
    }
    if (bounds.width > 0) {
      this.ratio.set(SplitPane.clamp(((event.clientX - bounds.left) / bounds.width) * PERCENT));
    }
  }

  protected endDrag(event: PointerEvent): void {
    if (!this.dragging()) {
      return;
    }
    this.dragging.set(false);
    this.bounds = null;
    const target = event.currentTarget as HTMLElement;
    if (target.hasPointerCapture(event.pointerId)) {
      target.releasePointerCapture(event.pointerId);
    }
    this.save();
  }

  protected onKey(event: KeyboardEvent): void {
    const next = SplitPane.keyRatio(event.key, this.ratio(), this.initialRatio());
    if (next === null) {
      return;
    }
    event.preventDefault();
    this.ratio.set(next);
    this.save();
  }

  private save(): void {
    this.store.set(this.storageKey(), this.ratio().toFixed(1));
  }
}
