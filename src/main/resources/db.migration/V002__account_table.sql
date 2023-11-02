create table account(
    id uuid default gen_random_uuid() primary key,
    balance numeric(20, 2) not null,
    currency_code text not null,
    created_at  timestamp without time zone not null,
    updated_at  timestamp without time zone not null
);

create trigger set_timestamp_update
    before update
    on account
    for each row
execute procedure trigger_set_timestamps_for_updated_entry();

create trigger set_timestamp_insert
    before insert
    on account
    for each row
execute procedure trigger_set_timestamps_for_new_entry();