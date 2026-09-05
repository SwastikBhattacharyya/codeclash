import type { HttpResourceRequest } from '@angular/common/http';
import { LOOKUP_PAGE_SIZE } from '../api/page';

export type Question = {
  id: string;
  title: string;
  summary: string;
  description: string;
  isVisible: boolean;
  maxRunTimeMs: number;
  maxRamLimitMb: number;
  categories: string[];
  tags: string[];
  createdAt: string;
  updatedAt: string;
};

export type PublicTestCase = {
  id: string;
  input: string;
  output: string;
  isPublic: boolean;
};

export type Category = {
  id: string;
  name: string;
  description: string;
};

export type Tag = {
  id: string;
  name: string;
  description: string;
};

export type QuestionQuery = {
  title: string;
  category: string | null;
  tag: string | null;
  page: number;
};

export const PROBLEM_PAGE_SIZE = 10;
export const QUESTIONS_URL = '/api/v1/questions';
export const CATEGORIES_URL = '/api/v1/categories';
export const TAGS_URL = '/api/v1/tags';
export const PROBLEMS_PATH = '/';
export const PROBLEM_PATH = '/problems';

export function questionsRequest(query: QuestionQuery): HttpResourceRequest {
  const params: Record<string, string> = {
    sort: 'title',
    direction: 'asc',
    page: String(query.page),
    size: String(PROBLEM_PAGE_SIZE),
  };
  if (query.title !== '') {
    params['title'] = query.title;
  }
  if (query.category !== null) {
    params['category'] = query.category;
  }
  if (query.tag !== null) {
    params['tag'] = query.tag;
  }
  return { url: QUESTIONS_URL, params };
}

export function questionsByIdsRequest(ids: readonly string[]): HttpResourceRequest {
  return { url: QUESTIONS_URL, params: { id: [...ids], size: String(LOOKUP_PAGE_SIZE) } };
}

export function questionUrl(id: string): string {
  return `${QUESTIONS_URL}/${encodeURIComponent(id)}`;
}

export function testCasesUrl(questionId: string): string {
  return `${questionUrl(questionId)}/test-cases`;
}

export function testCaseUrl(questionId: string, id: string): string {
  return `${testCasesUrl(questionId)}/${encodeURIComponent(id)}`;
}

export function lookupRequest(url: string): HttpResourceRequest {
  return { url, params: { sort: 'name', direction: 'asc', size: String(LOOKUP_PAGE_SIZE) } };
}
