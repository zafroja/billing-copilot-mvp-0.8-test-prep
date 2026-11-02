create table if not exists jobber_connection (
  id bigserial primary key,
  tenant_id bigint not null,
  account_id varchar(255),
  account_name varchar(255),
  access_token text,
  refresh_token text,
  access_token_expires_at timestamp with time zone,
  connected boolean not null default false,
  created_at timestamp with time zone not null default now(),
  updated_at timestamp with time zone not null default now()
);
create unique index if not exists ux_jobber_connection_tenant on jobber_connection(tenant_id);
