import { formatDate, isPlatformBrowser } from '@angular/common';
import { inject, LOCALE_ID, Pipe, type PipeTransform, PLATFORM_ID } from '@angular/core';

const UTC = 'UTC';

@Pipe({ name: 'localTime' })
export class LocalTime implements PipeTransform {
  private readonly locale = inject(LOCALE_ID);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  transform(value: string | null | undefined, format: string): string {
    if (!value) {
      return '';
    }
    if (!this.isBrowser) {
      return `${formatDate(value, format, this.locale, UTC)} ${UTC}`;
    }
    const zone =
      new Intl.DateTimeFormat(this.locale, { timeZoneName: 'short' })
        .formatToParts(new Date(value))
        .find((part) => part.type === 'timeZoneName')?.value ?? '';
    return `${formatDate(value, format, this.locale)} ${zone}`.trim();
  }
}
