create table sessions
(
  id           uuid                 default uuidv7(),
  user_id      uuid        not null,
  last_used_at timestamptz not null default now(),
  created_at   timestamptz not null default now(),
  updated_at   timestamptz not null default now(),

  constraint pk_sessions primary key (id),
  constraint ck_sessions_id_version check (uuid_extract_version(id) = 7),
  constraint fk_sessions_user
    foreign key (user_id) references users (id) on delete cascade
);

create index ix_sessions_user_id
  on sessions (user_id);

create trigger trg_sessions_set_updated_at
  before update
  on sessions
  for each row
  when (old is distinct from new)
execute function set_updated_at();
