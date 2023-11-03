create table money_booking_order(
    checkout_id uuid default gen_random_uuid() primary key,
    customer_id uuid references account (id) on delete restrict not null,
    tenant_id uuid references account (id) on delete restrict not null,
    status text not null default 'PENDING',
    amount numeric(20, 2) not null,
    currency_code text not null,
    created_at timestamp without time zone not null,
    updated_at timestamp without time zone not null,
    ledger_updated timestamp without time zone
);

create trigger set_timestamp_update
    before update
    on money_booking_order
    for each row
    execute procedure trigger_set_timestamps_for_updated_entry();

create trigger set_timestamp_insert
    before insert
    on money_booking_order
    for each row
    execute procedure trigger_set_timestamps_for_new_entry();

create index idx_money_booking_order_model_customer_id on money_booking_order (customer_id);
create index idx_money_booking_order_model_tenant_id on money_booking_order (tenant_id);