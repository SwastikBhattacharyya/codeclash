import type { HttpResourceRef } from '@angular/common/http';
import { linkedSignal, type Signal } from '@angular/core';

export function lastValue<T>(resource: HttpResourceRef<T | undefined>): Signal<T | undefined> {
  return linkedSignal<T | undefined, T | undefined>({
    source: () => (resource.hasValue() ? resource.value() : undefined),
    computation: (value, previous) => value ?? previous?.value,
  });
}
