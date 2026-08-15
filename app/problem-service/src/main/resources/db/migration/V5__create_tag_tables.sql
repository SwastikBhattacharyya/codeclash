create table tags
(
  id          uuid                 default uuidv7(),
  name        text        not null,
  description text        not null,
  created_at  timestamptz not null default now(),
  updated_at  timestamptz not null default now(),

  constraint pk_tags primary key (id),
  constraint ck_tags_id_version check (uuid_extract_version(id) = 7),
  constraint ck_tags_name_min_size check (char_length(name) >= 2),
  constraint ck_tags_name_max_size check (char_length(name) <= 64),
  constraint ck_tags_name_single_line check (name !~ E'[\n\r\u0085\u2028\u2029]'),
  constraint ck_tags_name_trimmed check (is_stripped(name)),
  constraint ck_tags_description_min_size check (char_length(description) >= 1),
  constraint ck_tags_description_max_size check (char_length(description) <= 255),
  constraint ck_tags_description_trimmed check (is_stripped(description))
);

create unique index uk_tags_name
  on tags (lower(name));

create trigger trg_tags_set_updated_at
  before update
  on tags
  for each row
  when (old is distinct from new)
execute function set_updated_at();
