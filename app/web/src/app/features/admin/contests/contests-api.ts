import { HttpClient, type HttpResourceRequest } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import {
  CONTESTS_URL,
  type Contest,
  type ContestSummary,
  contestUrl,
} from '../../../core/contests/contests-api';
import type { SortDirection } from '../../../ui/data-table';

export type ContestStatus = 'upcoming' | 'running' | 'ended';

export type ContestSortField = 'name' | 'live_at' | 'end_at' | 'updated_at';

export type ContestQuery = {
  name: string;
  status: ContestStatus | null;
  liveFrom: string | null;
  liveTo: string | null;
  page: number;
  sort: ContestSortField;
  direction: SortDirection;
};

export type ContestQuestionInput = {
  id: string;
  points: number;
};

export type ContestInput = {
  name: string;
  description: string;
  liveAt: string;
  endAt: string;
  questions: ContestQuestionInput[];
};

export type ContestChanges = Partial<ContestInput>;

export const CONTEST_ADMIN_PAGE_SIZE = 20;
export const CONTEST_FORBIDDEN_MESSAGE = "You don't have permission to change this contest.";
export const CONTEST_ALREADY_LIVE = 'CONTEST_ALREADY_LIVE';
export const CONTEST_STATUSES: readonly ContestStatus[] = ['upcoming', 'running', 'ended'];
export const CONTEST_STATUS_LABELS: Readonly<Record<ContestStatus, string>> = {
  upcoming: 'Upcoming',
  running: 'Running',
  ended: 'Ended',
};
export const CONTEST_SORT_FIELDS: readonly ContestSortField[] = [
  'name',
  'live_at',
  'end_at',
  'updated_at',
];

export function contestStatus(contest: ContestSummary, now: number): ContestStatus {
  if (now >= Date.parse(contest.endAt)) {
    return 'ended';
  }
  return contestStarted(contest, now) ? 'running' : 'upcoming';
}

export function contestStarted(contest: ContestSummary, now: number): boolean {
  return contest.openedAt !== null || Date.parse(contest.liveAt) <= now;
}

export function contestsListRequest(query: ContestQuery, now: string): HttpResourceRequest {
  const params: Record<string, string> = {
    page: String(query.page),
    size: String(CONTEST_ADMIN_PAGE_SIZE),
    sort: query.sort,
    direction: query.direction,
  };
  if (query.name !== '') {
    params['name'] = query.name;
  }
  let liveLower = query.liveFrom;
  let liveUpper = query.liveTo;
  const justAfter = new Date(Date.parse(now) + 1).toISOString();
  if (query.status === 'upcoming') {
    liveLower = later(liveLower, justAfter);
  }
  if (query.status === 'running') {
    liveUpper = earlier(liveUpper, now);
    params['end_at_0'] = justAfter;
  }
  if (query.status === 'ended') {
    params['end_at_1'] = now;
  }
  if (liveLower !== null) {
    params['live_at_0'] = liveLower;
  }
  if (liveUpper !== null) {
    params['live_at_1'] = liveUpper;
  }
  return { url: CONTESTS_URL, params };
}

function later(bound: string | null, candidate: string): string {
  return bound !== null && Date.parse(bound) > Date.parse(candidate) ? bound : candidate;
}

function earlier(bound: string | null, candidate: string): string {
  return bound !== null && Date.parse(bound) < Date.parse(candidate) ? bound : candidate;
}

@Service()
export class AdminContestsApi {
  private readonly http = inject(HttpClient);

  create(input: ContestInput): Promise<Contest> {
    return firstValueFrom(this.http.post<Contest>(CONTESTS_URL, input));
  }

  update(id: string, changes: ContestChanges): Promise<Contest> {
    return firstValueFrom(this.http.patch<Contest>(contestUrl(id), changes));
  }

  get(id: string): Promise<Contest> {
    return firstValueFrom(this.http.get<Contest>(contestUrl(id)));
  }

  remove(id: string): Promise<void> {
    return firstValueFrom(this.http.delete<void>(contestUrl(id)));
  }
}
