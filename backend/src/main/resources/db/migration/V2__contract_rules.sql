create table if not exists contract_document (
  id bigserial primary key, tenant_id bigint not null references tenant(id), file_name varchar(255) not null,
  status varchar(40) not null default 'UPLOADED', created_at timestamptz not null default now()
);
create table if not exists contract_billing_rule (
  id bigserial primary key, contract_document_id bigint not null references contract_document(id), service varchar(160) not null,
  rate numeric(14,2) not null, unit varchar(60) not null, source_page integer, source_quote text, confidence numeric(5,4),
  human_verified boolean not null default false, created_at timestamptz not null default now()
);
