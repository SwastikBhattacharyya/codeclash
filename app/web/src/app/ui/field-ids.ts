import { Service } from '@angular/core';

@Service()
export class FieldIds {
  private next = 0;

  create(prefix: string): string {
    this.next += 1;
    return `cc-${prefix}-${this.next}`;
  }
}
