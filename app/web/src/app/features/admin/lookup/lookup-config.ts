import type { AdminSectionKey } from '../../../core/admin/admin-sections';
import { CATEGORIES_URL, TAGS_URL } from '../../../core/problems/problems-api';

export type LookupConfig = {
  section: AdminSectionKey;
  url: string;
  noun: string;
};

export const LOOKUP_DATA_KEY = 'lookup';

export const CATEGORY_LOOKUP: LookupConfig = {
  section: 'categories',
  url: CATEGORIES_URL,
  noun: 'category',
};

export const TAG_LOOKUP: LookupConfig = {
  section: 'tags',
  url: TAGS_URL,
  noun: 'tag',
};

export function capitalized(noun: string): string {
  return noun.charAt(0).toUpperCase() + noun.slice(1);
}
