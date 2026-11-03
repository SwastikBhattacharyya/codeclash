import { HttpClient, type HttpResourceRequest } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import type { SortDirection } from '../../../ui/data-table';

export type LookupItem = {
  id: string;
  name: string;
  description: string;
  createdAt: string;
  updatedAt: string;
};

export type LookupInput = {
  name: string;
  description: string;
};

export type LookupSortField = 'name' | 'created_at' | 'updated_at';

export type LookupQuery = {
  name: string;
  page: number;
  sort: LookupSortField;
  direction: SortDirection;
};

export const LOOKUP_ADMIN_PAGE_SIZE = 20;
export const LOOKUP_SORT_FIELDS: readonly LookupSortField[] = ['name', 'created_at', 'updated_at'];

export function lookupListRequest(url: string, query: LookupQuery): HttpResourceRequest {
  const params: Record<string, string> = {
    page: String(query.page),
    size: String(LOOKUP_ADMIN_PAGE_SIZE),
    sort: query.sort,
    direction: query.direction,
  };
  if (query.name !== '') {
    params['name'] = query.name;
  }
  return { url, params };
}

export function lookupItemUrl(url: string, id: string): string {
  return `${url}/${encodeURIComponent(id)}`;
}

@Service()
export class LookupApi {
  private readonly http = inject(HttpClient);

  create(url: string, input: LookupInput): Promise<LookupItem> {
    return firstValueFrom(this.http.post<LookupItem>(url, input));
  }

  update(url: string, id: string, input: LookupInput): Promise<LookupItem> {
    return firstValueFrom(this.http.patch<LookupItem>(lookupItemUrl(url, id), input));
  }

  remove(url: string, id: string): Promise<void> {
    return firstValueFrom(this.http.delete<void>(lookupItemUrl(url, id)));
  }
}
