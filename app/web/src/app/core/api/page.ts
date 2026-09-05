export type Page<T> = {
  data: T[];
  page: number;
  size: number;
  items: number;
  hasNext: boolean;
};

export const LOOKUP_PAGE_SIZE = 100;
