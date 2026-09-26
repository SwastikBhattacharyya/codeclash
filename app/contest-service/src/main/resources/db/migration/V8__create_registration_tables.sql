create table registrations
(
  contest_id    uuid        not null,
  user_id       uuid        not null,
  registered_at timestamptz not null default now(),

  constraint pk_registrations primary key (contest_id, user_id),
  constraint fk_registrations_contest foreign key (contest_id)
    references contests (id) on delete cascade,
  constraint fk_registrations_user foreign key (user_id)
    references users (id) on delete cascade
);

create index ix_registrations_user_id
  on registrations (user_id);
