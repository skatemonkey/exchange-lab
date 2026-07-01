begin;

truncate table trades, orders, stock_positions, trader_accounts cascade;

insert into trader_accounts (trader_id, cash_balance, reserved_cash)
select
    ('00000000-0000-0000-0000-' || lpad(i::text, 12, '0'))::uuid,
    100000000.00000000,
    0.00000000
from generate_series(1, 1000) as i;

insert into trader_accounts (trader_id, cash_balance, reserved_cash)
select
    ('00000000-0000-0000-0001-' || lpad(i::text, 12, '0'))::uuid,
    0.00000000,
    0.00000000
from generate_series(1, 500) as i;

insert into stock_positions (position_id, trader_id, symbol, quantity, reserved_quantity)
select
    ('10000000-0000-0000-0001-' || lpad(i::text, 12, '0'))::uuid,
    ('00000000-0000-0000-0001-' || lpad(i::text, 12, '0'))::uuid,
    'ACME',
    100000.00000000,
    100000.00000000
from generate_series(1, 500) as i;

insert into orders (
    order_id,
    trader_id,
    symbol,
    side,
    limit_price,
    quantity,
    remaining_quantity,
    status,
    created_at
)
select
    ('20000000-0000-0000-0001-' || lpad(i::text, 12, '0'))::uuid,
    ('00000000-0000-0000-0001-' || lpad(i::text, 12, '0'))::uuid,
    'ACME',
    'SELL',
    100.00000000,
    100000.00000000,
    100000.00000000,
    'ACCEPTED',
    timestamp with time zone '2026-01-01 00:00:00+00' + (i || ' seconds')::interval
from generate_series(1, 500) as i;

commit;
