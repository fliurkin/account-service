create or replace function public.trigger_set_timestamps_for_new_entry()
    returns trigger as
$$
declare
    now_timestamp timestamp := now();
begin
    new.created_at = now_timestamp;
    new.updated_at = now_timestamp;
    return new;
end;
$$ language plpgsql;

create or replace function public.trigger_set_timestamps_for_updated_entry()
    returns trigger as
$$
begin
    new.created_at = old.created_at;
    new.updated_at = now();
    return new;
end;
$$ language plpgsql;
