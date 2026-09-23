create table users
(
  id         uuid        not null,
  username   text        not null,
  created_at timestamptz not null,
  updated_at timestamptz not null,

  constraint pk_users primary key (id),
  constraint ck_users_id_version check (uuid_extract_version(id) = 7)
);

create unique index uk_users_username
  on users (username);
