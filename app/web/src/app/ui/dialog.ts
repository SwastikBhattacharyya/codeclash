import {
  afterNextRender,
  Component,
  DestroyRef,
  DOCUMENT,
  type ElementRef,
  inject,
  input,
  output,
  viewChild,
} from '@angular/core';
import { FieldIds } from './field-ids';

@Component({
  selector: 'cc-dialog',
  template: `
    <dialog
      #dialog
      [attr.aria-labelledby]="titleId"
      [attr.aria-describedby]="describedBy()"
      (cancel)="onCancel($event)"
      (close)="onClose()"
      [class.max-w-lg]="!wide()"
      [class.max-w-3xl]="wide()"
      class="m-auto w-[calc(100%-2rem)] rounded-panel border border-line bg-raised p-0 text-ink shadow-overlay backdrop:bg-board/60"
    >
      <div class="flex flex-col gap-5 p-6">
        <h2 [id]="titleId" class="text-xl text-ink">{{ title() }}</h2>
        <ng-content />
      </div>
    </dialog>
  `,
})
export class Dialog {
  readonly title = input.required<string>();
  readonly describedBy = input<string | null>(null);
  readonly wide = input(false);
  readonly closed = output<void>();

  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');
  private readonly document = inject(DOCUMENT);

  protected readonly titleId = inject(FieldIds).create('dialog-title');
  private destroyed = false;

  constructor() {
    const destroyRef = inject(DestroyRef);
    afterNextRender(() => {
      const element = this.dialog().nativeElement;
      const active = this.document.activeElement;
      const previous = active instanceof HTMLElement ? active : null;
      element.showModal();
      destroyRef.onDestroy(() => {
        this.destroyed = true;
        if (element.open) {
          element.close();
        }
        if (previous?.isConnected) {
          previous.focus();
        }
      });
    });
  }

  protected onCancel(event: Event): void {
    event.preventDefault();
    this.closed.emit();
  }

  protected onClose(): void {
    if (this.destroyed) {
      return;
    }
    this.dialog().nativeElement.showModal();
    this.closed.emit();
  }
}
