import { Component, inject } from '@angular/core';
import { LucideMonitor, LucideMoon, LucideSun } from '@lucide/angular';
import { type ThemePreference, ThemeStore } from '../core/theme/theme-store';
import { FieldIds } from './field-ids';

type ThemeOption = {
  value: ThemePreference;
  label: string;
};

@Component({
  selector: 'cc-theme-toggle',
  imports: [LucideMonitor, LucideMoon, LucideSun],
  templateUrl: './theme-toggle.html',
})
export class ThemeToggle {
  protected readonly store = inject(ThemeStore);
  protected readonly groupName = inject(FieldIds).create('theme');
  protected readonly options: readonly ThemeOption[] = [
    { value: 'system', label: 'System theme' },
    { value: 'light', label: 'Light theme' },
    { value: 'dark', label: 'Dark theme' },
  ];
}
