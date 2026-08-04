import { NgTemplateOutlet } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import {
  LucideCircleCheck,
  LucideCircleExclamationPoint,
  LucideInfo,
  LucideX,
} from '@lucide/angular';
import { ToastStore } from './toast-store';

@Component({
  selector: 'cc-toast-outlet',
  imports: [NgTemplateOutlet, LucideCircleCheck, LucideCircleExclamationPoint, LucideInfo, LucideX],
  templateUrl: './toast-outlet.html',
})
export class ToastOutlet {
  protected readonly store = inject(ToastStore);
  protected readonly errors = computed(() =>
    this.store.toasts().filter((toast) => toast.tone === 'error'),
  );
  protected readonly notices = computed(() =>
    this.store.toasts().filter((toast) => toast.tone !== 'error'),
  );
}
