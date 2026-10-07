create table contests
(
  id         uuid        not null,
  live_at    timestamptz not null,
  end_at     timestamptz not null,
  opened_at  timestamptz,
  created_at timestamptz not null,
  updated_at timestamptz not null,

  constraint pk_contests primary key (id),
  constraint ck_contests_id_version check (uuid_extract_version(id) = 7)
);
