create table leaderboard_entries
(
  contest_id    uuid        not null,
  user_id       uuid        not null,
  points        int         not null,
  solved        int         not null,
  last_solve_at timestamptz,
  rank          int         not null,

  constraint pk_leaderboard_entries primary key (contest_id, user_id),
  constraint fk_leaderboard_entries_registration foreign key (contest_id, user_id)
    references registrations (contest_id, user_id) on delete cascade
);

create index ix_leaderboard_entries_contest_id_rank_user_id
  on leaderboard_entries (contest_id, rank, user_id);
