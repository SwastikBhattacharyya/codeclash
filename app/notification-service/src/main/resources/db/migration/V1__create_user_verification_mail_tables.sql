create table user_verification_mails
(
  id                 uuid                 default uuidv7(),
  user_id            uuid        not null,
  verification_token uuid        not null,
  email              text        not null,
  sent_at            timestamptz not null default now(),

  constraint pk_user_verification_mails primary key (id),
  constraint ck_user_verification_mails_id_version check (uuid_extract_version(id) = 7),
  constraint ck_user_verification_mails_user_id_version check (
    uuid_extract_version(user_id) = 7
  ),
  constraint ck_user_verification_mails_verification_token_version check (
    uuid_extract_version(verification_token) = 4
  ),
  constraint ck_user_verification_mails_email_not_blank check (email ~ '\S')
);

create unique index uk_user_verification_mails_verification_token
  on user_verification_mails (verification_token);

create index ix_user_verification_mails_user_id
  on user_verification_mails (user_id);
