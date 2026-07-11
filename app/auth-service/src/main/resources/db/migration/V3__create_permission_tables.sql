create table permissions
(
  id          uuid                 default uuidv7(),
  name        text        not null,
  description text        not null,
  created_at  timestamptz not null default now(),
  updated_at  timestamptz not null default now(),

  constraint pk_permissions primary key (id),
  constraint uk_permissions_name unique (name),
  constraint ck_permissions_id_version check (uuid_extract_version(id) = 7),
  constraint ck_permissions_name_max_size check (char_length(name) <= 255),
  constraint ck_permissions_name_format check (
    name ~ '^[a-z]+(_[a-z]+)*(:[a-z]+(_[a-z]+)*){2}$'
  ),
  constraint ck_permissions_description_min_size check (char_length(description) >= 1),
  constraint ck_permissions_description_max_size check (char_length(description) <= 255),
  constraint ck_permissions_description_trimmed check (is_stripped(description))
);

create trigger trg_permissions_set_updated_at
  before update
  on permissions
  for each row
  when (old is distinct from new)
execute function set_updated_at();
