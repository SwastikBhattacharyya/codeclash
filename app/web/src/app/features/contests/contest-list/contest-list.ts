import { httpResource } from '@angular/common/http';
import { Component, computed, inject, makeStateKey, TransferState } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { lastValue } from '../../../core/api/last-value';
import type { Page } from '../../../core/api/page';
import { excerpt } from '../../../core/contests/contest-text';
import {
  CONTESTS_PATH,
  type ContestSummary,
  liveContestsRequest,
  pastContestsRequest,
  upcomingContestsRequest,
} from '../../../core/contests/contests-api';
import { Clock, describeDuration, minuteStamp } from '../../../core/time/clock';
import { LocalTime } from '../../../core/time/local-time';
import { Alert } from '../../../ui/alert';
import { Button } from '../../../ui/button';
import { Countdown } from '../../../ui/countdown';
import { Spinner } from '../../../ui/spinner';

const FIRST_PAGE = 1;
const NOW_KEY = makeStateKey<string>('contest-list-now');

@Component({
  selector: 'cc-contest-list',
  imports: [LocalTime, RouterLink, Alert, Button, Countdown, Spinner],
  templateUrl: './contest-list.html',
})
export class ContestList {
  private readonly now = this.sharedNow();
  private readonly params = toSignal(inject(ActivatedRoute).queryParamMap, {
    requireSync: true,
  });

  protected readonly contestsPath = CONTESTS_PATH;
  protected readonly pastPage = computed(() => {
    const page = Number.parseInt(this.params().get('page') ?? '', 10);
    return Number.isFinite(page) && page >= FIRST_PAGE ? page : FIRST_PAGE;
  });
  protected readonly live = httpResource<Page<ContestSummary>>(() => liveContestsRequest(this.now));
  protected readonly upcoming = httpResource<Page<ContestSummary>>(() =>
    upcomingContestsRequest(this.now),
  );
  protected readonly past = httpResource<Page<ContestSummary>>(() =>
    pastContestsRequest(this.now, this.pastPage()),
  );
  protected readonly liveShown = lastValue(this.live);
  protected readonly upcomingShown = lastValue(this.upcoming);
  protected readonly pastShown = lastValue(this.past);
  protected readonly loaded = computed(
    () => !!(this.liveShown() && this.upcomingShown() && this.pastShown()),
  );
  protected readonly failed = computed(
    () => !this.loaded() && !!(this.live.error() || this.upcoming.error() || this.past.error()),
  );
  protected readonly empty = computed(
    () =>
      this.loaded() &&
      this.pastPage() === FIRST_PAGE &&
      (this.liveShown()?.data.length ?? 0) === 0 &&
      (this.upcomingShown()?.data.length ?? 0) === 0 &&
      (this.pastShown()?.data.length ?? 0) === 0,
  );

  protected excerpt(contest: ContestSummary): string {
    return excerpt(contest.description);
  }

  protected duration(contest: ContestSummary): string {
    return describeDuration(Date.parse(contest.endAt) - Date.parse(contest.liveAt));
  }

  protected pageParams(page: number): Record<string, string | null> {
    return { page: page === FIRST_PAGE ? null : String(page) };
  }

  private sharedNow(): string {
    const transferState = inject(TransferState);
    const stored = transferState.get(NOW_KEY, null);
    if (stored !== null) {
      return stored;
    }
    const now = minuteStamp(inject(Clock).now());
    transferState.set(NOW_KEY, now);
    return now;
  }
}
