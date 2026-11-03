import {
  Component,
  computed,
  Injector,
  inject,
  input,
  linkedSignal,
  output,
  signal,
} from '@angular/core';
import { FormField, FormRoot, form } from '@angular/forms/signals';
import { isGone } from '../../../core/api/gone';
import {
  focusFirstInvalid,
  focusFirstInvalidAfterRender,
  toSubmitFailure,
} from '../../../core/forms/form-errors';
import { Alert } from '../../../ui/alert';
import { Button } from '../../../ui/button';
import { Dialog } from '../../../ui/dialog';
import { TextArea } from '../../../ui/text-area';
import { TextField } from '../../../ui/text-field';
import { LookupApi, type LookupItem } from './lookup-api';
import type { LookupConfig } from './lookup-config';
import {
  LOOKUP_DESCRIPTION_MAX_SIZE,
  lookupDescriptionRules,
  lookupNameRules,
} from './lookup-rules';

type LookupModel = {
  name: string;
  description: string;
};

export type LookupSaved = {
  item: LookupItem;
  created: boolean;
};

@Component({
  selector: 'cc-lookup-form-dialog',
  imports: [FormRoot, FormField, Alert, Button, Dialog, TextArea, TextField],
  template: `
    <cc-dialog [title]="title()" (closed)="close()">
      <form [formRoot]="lookupForm" class="flex flex-col gap-5">
        @if (formError(); as message) {
          <cc-alert tone="error">{{ message }}</cc-alert>
        }
        <cc-text-field [formField]="lookupForm.name" label="Name" />
        <cc-text-area
          [formField]="lookupForm.description"
          label="Description"
          [limit]="descriptionMaxSize"
        />
        <div class="flex flex-wrap justify-end gap-3">
          <button
            ccButton
            variant="ghost"
            type="button"
            [disabled]="lookupForm().submitting()"
            (click)="close()"
          >
            Cancel
          </button>
          <button ccButton type="submit" [loading]="lookupForm().submitting()">
            {{ submitLabel() }}
          </button>
        </div>
      </form>
    </cc-dialog>
  `,
})
export class LookupFormDialog {
  private readonly api = inject(LookupApi);
  private readonly injector = inject(Injector);

  readonly config = input.required<LookupConfig>();
  readonly item = input<LookupItem | null>(null);
  readonly saved = output<LookupSaved>();
  readonly gone = output<void>();
  readonly closed = output<void>();

  protected readonly descriptionMaxSize = LOOKUP_DESCRIPTION_MAX_SIZE;
  protected readonly title = computed(() =>
    this.item() ? `Edit ${this.config().noun}` : `New ${this.config().noun}`,
  );
  protected readonly submitLabel = computed(() =>
    this.item() ? 'Save changes' : `Create ${this.config().noun}`,
  );
  protected readonly formError = signal<string | null>(null);
  private readonly model = linkedSignal<LookupModel>(() => ({
    name: this.item()?.name ?? '',
    description: this.item()?.description ?? '',
  }));
  protected readonly lookupForm = form(
    this.model,
    (path) => {
      lookupNameRules(path.name);
      lookupDescriptionRules(path.description);
    },
    {
      submission: {
        action: async (field) => {
          this.formError.set(null);
          const value = field().value();
          const changes = { name: value.name.trim(), description: value.description.trim() };
          const current = this.item();
          const url = this.config().url;
          try {
            const item = current
              ? await this.api.update(url, current.id, changes)
              : await this.api.create(url, changes);
            this.saved.emit({ item, created: current === null });
            return undefined;
          } catch (error) {
            if (current && isGone(error)) {
              this.gone.emit();
              return undefined;
            }
            const failure = toSubmitFailure(error, {
              name: field.name,
              description: field.description,
            });
            this.formError.set(failure.formError);
            if (failure.fieldErrors.length > 0) {
              focusFirstInvalidAfterRender(this.injector, [field.name, field.description]);
            }
            return failure.fieldErrors;
          }
        },
        onInvalid: (field) => focusFirstInvalid([field.name, field.description]),
      },
    },
  );

  protected close(): void {
    if (!this.lookupForm().submitting()) {
      this.closed.emit();
    }
  }
}
