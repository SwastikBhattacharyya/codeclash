import { Component, input } from '@angular/core';
import {
  LucideFileCode,
  LucideFolderTree,
  LucideKeyRound,
  LucideListChecks,
  LucideTags,
  LucideTrophy,
  LucideUsers,
} from '@lucide/angular';
import type { AdminSectionKey } from '../core/admin/admin-sections';

@Component({
  selector: 'cc-admin-section-icon',
  imports: [
    LucideFileCode,
    LucideFolderTree,
    LucideKeyRound,
    LucideListChecks,
    LucideTags,
    LucideTrophy,
    LucideUsers,
  ],
  host: { class: 'inline-flex shrink-0' },
  template: `
    @switch (section()) {
      @case ('questions') {
        <svg lucideFileCode class="size-full"></svg>
      }
      @case ('tags') {
        <svg lucideTags class="size-full"></svg>
      }
      @case ('categories') {
        <svg lucideFolderTree class="size-full"></svg>
      }
      @case ('submissions') {
        <svg lucideListChecks class="size-full"></svg>
      }
      @case ('contests') {
        <svg lucideTrophy class="size-full"></svg>
      }
      @case ('users') {
        <svg lucideUsers class="size-full"></svg>
      }
      @case ('roles') {
        <svg lucideKeyRound class="size-full"></svg>
      }
    }
  `,
})
export class AdminSectionIcon {
  readonly section = input.required<AdminSectionKey>();
}
