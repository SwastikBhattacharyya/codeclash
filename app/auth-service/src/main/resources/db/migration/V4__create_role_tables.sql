create table roles
(
  id          uuid                 default uuidv7(),
  name        text        not null,
  description text        not null,
  is_default  bool        not null default false,
  created_at  timestamptz not null default now(),
  updated_at  timestamptz not null default now(),

  constraint pk_roles primary key (id),
  constraint uk_roles_name unique (name),
  constraint ck_roles_id_version check (uuid_extract_version(id) = 7),
  constraint ck_roles_name_max_size check (char_length(name) <= 255),
  constraint ck_roles_name_format check (name ~ '^[a-z]+(_[a-z]+)*$'),
  constraint ck_roles_description_min_size check (char_length(description) >= 1),
  constraint ck_roles_description_max_size check (char_length(description) <= 255),
  constraint ck_roles_description_trimmed check (is_stripped(description))
);

create trigger trg_roles_set_updated_at
  before update
  on roles
  for each row
  when (old is distinct from new)
execute function set_updated_at();
