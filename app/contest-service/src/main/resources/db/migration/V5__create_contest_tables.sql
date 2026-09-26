create table contests
(
  id          uuid                 default uuidv7(),
  name        text        not null,
  description text        not null,
  live_at     timestamptz not null,
  end_at      timestamptz not null,
  opened_at   timestamptz,
  created_at  timestamptz not null default now(),
  updated_at  timestamptz not null default now(),

  constraint pk_contests primary key (id),
  constraint ck_contests_id_version check (uuid_extract_version(id) = 7),
  constraint ck_contests_name_min_size check (char_length(name) >= 8),
  constraint ck_contests_name_max_size check (char_length(name) <= 64),
  constraint ck_contests_name_single_line check (name !~ E'[\n\r\u0085\u2028\u2029]'),
  constraint ck_contests_name_trimmed check (is_stripped(name)),
  constraint ck_contests_description_min_size check (char_length(description) >= 32),
  constraint ck_contests_description_max_size check (char_length(description) <= 65535),
  constraint ck_contests_description_trimmed check (is_stripped(description)),
  constraint ck_contests_end_at_after_live_at check (end_at > live_at)
);

create unique index uk_contests_name
  on contests (lower(name));

create index ix_contests_live_at_unopened
  on contests (live_at)
  where opened_at is null;

create trigger trg_contests_set_updated_at
  before update
  on contests
  for each row
  when (old is distinct from new)
execute function set_updated_at();
