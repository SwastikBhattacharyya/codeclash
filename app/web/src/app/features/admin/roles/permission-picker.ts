import { Component, computed, inject, input, model, signal } from '@angular/core';
import { LucideCheck, LucideMinus } from '@lucide/angular';
import { FieldIds } from '../../../ui/field-ids';
import { TextField } from '../../../ui/text-field';
import type { Permission } from './roles-api';

type PermissionGroup = {
  key: string;
  label: string;
  permissions: Permission[];
};

type GroupState = 'all' | 'some' | 'none';

@Component({
  selector: 'cc-permission-picker',
  imports: [LucideCheck, LucideMinus, TextField],
  host: { class: 'flex flex-col gap-5', '(keydown.enter)': '$event.preventDefault()' },
  templateUrl: './permission-picker.html',
})
export class PermissionPicker {
  readonly permissions = input.required<readonly Permission[]>();
  readonly selected = model<readonly string[]>([]);
  readonly readonly = input(false);
  readonly disabled = input(false);

  protected readonly idPrefix = inject(FieldIds).create('permission-group');
  protected readonly filter = signal('');
  protected readonly filtering = computed(() => this.filter().trim() !== '');
  protected readonly selectedSet = computed(() => new Set(this.selected()));
  protected readonly selectedCount = computed(() => {
    const selected = this.selectedSet();
    return this.permissions().filter((permission) => selected.has(permission.id)).length;
  });
  protected readonly groups = computed<PermissionGroup[]>(() => {
    const term = this.filter().trim().toLowerCase();
    const selected = this.selectedSet();
    const groups = new Map<string, Permission[]>();
    for (const permission of this.permissions()) {
      if (this.readonly() && !selected.has(permission.id)) {
        continue;
      }
      if (
        term !== '' &&
        !permission.name.toLowerCase().includes(term) &&
        !permission.description.toLowerCase().includes(term)
      ) {
        continue;
      }
      const key = permission.name.split(':', 1)[0] ?? permission.name;
      groups.set(key, [...(groups.get(key) ?? []), permission]);
    }
    return [...groups.entries()]
      .sort(([left], [right]) => left.localeCompare(right))
      .map(([key, permissions]) => ({
        key,
        label: key.charAt(0).toUpperCase() + key.slice(1),
        permissions,
      }));
  });

  protected readonly filterStatus = computed(() => {
    if (!this.filtering()) {
      return '';
    }
    const count = this.groups().reduce((total, group) => total + group.permissions.length, 0);
    return count === 1 ? '1 permission shown' : `${count} permissions shown`;
  });

  protected isSelected(permission: Permission): boolean {
    return this.selectedSet().has(permission.id);
  }

  protected groupState(group: PermissionGroup): GroupState {
    const selected = this.selectedSet();
    const count = group.permissions.filter((permission) => selected.has(permission.id)).length;
    if (count === 0) {
      return 'none';
    }
    return count === group.permissions.length ? 'all' : 'some';
  }

  protected toggle(permission: Permission, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    this.apply([permission.id], checked);
  }

  protected toggleGroup(group: PermissionGroup, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    this.apply(
      group.permissions.map((permission) => permission.id),
      checked,
    );
  }

  private apply(ids: readonly string[], checked: boolean): void {
    const next = new Set(this.selected());
    for (const id of ids) {
      if (checked) {
        next.add(id);
      } else {
        next.delete(id);
      }
    }
    this.selected.set([...next]);
  }
}
