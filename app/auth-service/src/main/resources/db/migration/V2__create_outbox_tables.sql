create table outbox_records
(
  id             text        not null,
  status         text        not null,
  record_key     text        not null,
  record_type    text        not null,
  payload        text        not null,
  context        text,
  created_at     timestamptz not null,
  completed_at   timestamptz,
  failure_count  int         not null,
  failure_reason text,
  next_retry_at  timestamptz not null,
  partition_no   int         not null,
  handler_id     text        not null,

  constraint pk_outbox_records primary key (id)
);

create table outbox_instances
(
  instance_id    text        not null,
  hostname       text        not null,
  port           int         not null,
  status         text        not null,
  started_at     timestamptz not null,
  last_heartbeat timestamptz not null,
  created_at     timestamptz not null,
  updated_at     timestamptz not null,

  constraint pk_outbox_instances primary key (instance_id)
);

create table outbox_partitions
(
  partition_number int         not null,
  instance_id      text,
  version          bigint      not null default 0,
  updated_at       timestamptz not null,

  constraint pk_outbox_partitions primary key (partition_number)
);

create index idx_outbox_records_record_key_created
  on outbox_records (record_key, created_at);

create index idx_outbox_records_partition_status_retry
  on outbox_records (partition_no, status, next_retry_at);

create index idx_outbox_records_status_retry
  on outbox_records (status, next_retry_at);

create index idx_outbox_records_status
  on outbox_records (status);

create index idx_outbox_records_record_key_completed_created
  on outbox_records (record_key, completed_at, created_at);

create index idx_outbox_instances_status_heartbeat
  on outbox_instances (status, last_heartbeat);

create index idx_outbox_instances_last_heartbeat
  on outbox_instances (last_heartbeat);

create index idx_outbox_instances_status
  on outbox_instances (status);

create index idx_outbox_partitions_instance_id
  on outbox_partitions (instance_id);
