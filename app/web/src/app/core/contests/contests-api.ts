import { HttpClient, type HttpResourceRequest } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { solvedRequest } from '../submissions/submissions';

export type ContestSummary = {
  id: string;
  name: string;
  description: string;
  liveAt: string;
  endAt: string;
  openedAt: string | null;
  createdAt: string;
  updatedAt: string;
};

export type ContestQuestion = {
  id: string;
  points: number;
};

export type Contest = ContestSummary & {
  questions: ContestQuestion[] | null;
  registered: boolean | null;
};

export type LeaderboardEntry = {
  rank: number;
  username: string;
  points: number;
  solved: number;
  lastSolveAt: string | null;
};

export type ContestPhase = 'upcoming' | 'live' | 'ended';

export const CONTESTS_URL = '/api/v1/contests';
export const CONTESTS_PATH = '/contests';
export const CONTEST_SECTION_SIZE = 20;
export const LEADERBOARD_PAGE_SIZE = 50;
export const CONTEST_QUERY_PARAM = 'contest';

const LETTER_COUNT = 26;
const FIRST_LETTER_CODE = 65;

export function contestUrl(id: string): string {
  return `${CONTESTS_URL}/${encodeURIComponent(id)}`;
}

export function leaderboardRequest(id: string, page: number): HttpResourceRequest {
  return {
    url: `${contestUrl(id)}/leaderboard`,
    params: { page: String(page), size: String(LEADERBOARD_PAGE_SIZE) },
  };
}

export function liveContestsRequest(now: string): HttpResourceRequest {
  return {
    url: CONTESTS_URL,
    params: {
      live_at_1: now,
      end_at_0: now,
      sort: 'end_at',
      direction: 'asc',
      size: String(CONTEST_SECTION_SIZE),
    },
  };
}

export function upcomingContestsRequest(now: string): HttpResourceRequest {
  return {
    url: CONTESTS_URL,
    params: {
      live_at_0: now,
      sort: 'live_at',
      direction: 'asc',
      size: String(CONTEST_SECTION_SIZE),
    },
  };
}

export function pastContestsRequest(now: string, page: number): HttpResourceRequest {
  return {
    url: CONTESTS_URL,
    params: {
      end_at_1: now,
      sort: 'end_at',
      direction: 'desc',
      page: String(page),
      size: String(CONTEST_SECTION_SIZE),
    },
  };
}

export function contestPhase(contest: ContestSummary, now: number): ContestPhase {
  if (now < Date.parse(contest.liveAt)) {
    return 'upcoming';
  }
  return now < Date.parse(contest.endAt) ? 'live' : 'ended';
}

export function contestLetter(index: number): string {
  let remaining = index;
  let letters = '';
  do {
    letters = String.fromCharCode(FIRST_LETTER_CODE + (remaining % LETTER_COUNT)) + letters;
    remaining = Math.floor(remaining / LETTER_COUNT) - 1;
  } while (remaining >= 0);
  return letters;
}

export type ContestSolvedQuery = {
  userId: string;
  questionIds: readonly string[];
  from: string;
  until: string;
};

export function contestSolvedQuery(
  userId: string | null,
  contest: Contest | null | undefined,
): ContestSolvedQuery | null {
  const questionIds = contest?.questions?.map((question) => question.id) ?? [];
  if (userId === null || !contest || questionIds.length === 0) {
    return null;
  }
  return { userId, questionIds, from: contest.liveAt, until: contest.endAt };
}

export function sameContestSolvedQuery(
  left: ContestSolvedQuery | null,
  right: ContestSolvedQuery | null,
): boolean {
  if (left === null || right === null) {
    return left === right;
  }
  return (
    left.userId === right.userId &&
    left.from === right.from &&
    left.until === right.until &&
    left.questionIds.length === right.questionIds.length &&
    left.questionIds.every((id, index) => id === right.questionIds[index])
  );
}

export function contestSolvedRequest(
  query: ContestSolvedQuery | null,
): HttpResourceRequest | undefined {
  return query === null
    ? undefined
    : solvedRequest(query.userId, query.questionIds, { from: query.from, until: query.until });
}

@Service()
export class ContestsApi {
  private readonly http = inject(HttpClient);

  register(id: string): Promise<unknown> {
    return firstValueFrom(this.http.post(`${contestUrl(id)}/registration`, null));
  }

  unregister(id: string): Promise<unknown> {
    return firstValueFrom(this.http.delete(`${contestUrl(id)}/registration`));
  }
}
