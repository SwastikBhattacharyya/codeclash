create table users
(
  id                 uuid                 default uuidv7(),
  name               text        not null,
  username           text        not null,
  email              text        not null,
  password_hash      text        not null,
  is_verified        bool        not null,
  verification_token uuid                 default gen_random_uuid(),
  role_id            uuid,
  created_at         timestamptz not null default now(),
  updated_at         timestamptz not null default now(),

  constraint pk_users primary key (id),
  constraint ck_users_id_version check (uuid_extract_version(id) = 7),
  constraint ck_users_name_min_size check (char_length(name) >= 2),
  constraint ck_users_name_max_size check (char_length(name) <= 255),
  constraint ck_users_name_letters check (
    name ~ '[a-zA-Z].*[a-zA-Z]' and name !~ E'[\n\r\u0085\u2028\u2029]'
  ),
  constraint ck_users_name_trimmed check (is_stripped(name)),
  constraint ck_users_username_min_size check (char_length(username) >= 3),
  constraint ck_users_username_max_size check (char_length(username) <= 32),
  constraint ck_users_username_format check (username ~ '^[A-Za-z0-9_-]+$'),
  constraint ck_users_username_trimmed check (is_stripped(username)),
  constraint ck_users_email_max_size check (char_length(email) <= 254),
  constraint ck_users_email_format check (
    email ~ '^[^@ \t\n\v\f\r]+@[^@ \t\n\v\f\r]+$'
  ),
  constraint ck_users_email_trimmed check (is_stripped(email)),
  constraint ck_users_password_hash_not_blank check (password_hash ~ '\S'),
  constraint ck_users_verification_token_version check (
    uuid_extract_version(verification_token) = 4
  ),
  constraint ck_users_verification_token_verified check (
    (verification_token is null) = is_verified
  ),
  constraint fk_users_role
    foreign key (role_id) references roles (id) on delete set null
);

create unique index uk_users_email_lower
  on users (lower(email));

create unique index uk_users_username
  on users (username);

create unique index uk_users_verification_token
  on users (verification_token);

create index ix_users_role_id
  on users (role_id);

create trigger trg_users_set_updated_at
  before update
  on users
  for each row
  when (old is distinct from new)
execute function set_updated_at();

create function set_users_verification_token()
  returns trigger
  language plpgsql
as
$$
begin
  if new.is_verified then
    new.verification_token = null;
  elsif tg_op = 'INSERT' then
    new.verification_token = coalesce(new.verification_token, gen_random_uuid());
  elsif old.is_verified then
    new.verification_token = gen_random_uuid();
  else
    new.verification_token = old.verification_token;
  end if;
  return new;
end;
$$;

create trigger trg_users_set_verification_token
  before insert or update
  on users
  for each row
execute function set_users_verification_token();
