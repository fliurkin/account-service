create table audit
(
    table_name regclass                 not null,
    userid     text                     not null,
    action     text                     not null,
    log_time   timestamp with time zone not null
);

create or replace function audit_tables() returns trigger as
$audit$
begin
    if (tg_op = 'DELETE') then
        insert into audit values (tg_relid::regclass, user, tg_op, now());
    elsif (tg_op = 'UPDATE') then
        insert into audit values (tg_relid::regclass, user, tg_op, now());
    elsif (tg_op = 'INSERT') then
        insert into audit values (tg_relid::regclass, user, tg_op, now());
    end if;
    return null;
end;
$audit$ language plpgsql;

create trigger account_audit
    after insert or update or delete
    on account
    for each row
execute function audit_tables();

create trigger money_booking_order_audit
    after insert or update or delete
    on money_booking_order
    for each row
execute function audit_tables();

create trigger ledger_entry_audit
    after insert or update or delete
    on ledger_entry
    for each row
execute function audit_tables();