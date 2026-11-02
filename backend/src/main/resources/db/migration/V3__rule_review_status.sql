alter table contract_billing_rule add column if not exists review_status varchar(20) not null default 'PENDING';
update contract_billing_rule set review_status=case when human_verified then 'APPROVED' else 'PENDING' end;
