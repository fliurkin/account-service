create table ledger_entry(
    id uuid default gen_random_uuid() primary key,
    amount numeric(20,2) not null,
    checkout_id uuid references money_booking_order (checkout_id) on delete restrict not null,
    credit uuid references account (id) on delete restrict not null,
    debit uuid references account (id) on delete restrict not null,
    created_at timestamp without time zone not null,
    updated_at timestamp without time zone not null
);

create index idx_ledger_credit on ledger_entry (credit);
create index idx_ledger_debit on ledger_entry (debit);

create trigger set_timestamp_update
    before update
    on ledger_entry
    for each row
execute procedure trigger_set_timestamps_for_updated_entry();

create trigger set_timestamp_insert
    before insert
    on ledger_entry
    for each row
execute procedure trigger_set_timestamps_for_new_entry();

create rule ledger_entry_update_restriction as on update to ledger_entry
    do instead nothing;
create rule ledger_entry_delete_restriction as on delete to ledger_entry
    do instead nothing;