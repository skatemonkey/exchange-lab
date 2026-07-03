begin;

truncate table trades, orders, stock_positions, trader_accounts;

insert into trader_accounts (trader_id, cash_balance, reserved_cash)
select
    ('00000000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid,
    100000000.00000000,
    0.00000000
from generate_series(1, 100) as n;

insert into trader_accounts (trader_id, cash_balance, reserved_cash)
select
    ('00000000-0000-0000-0000-' || lpad((1000 + n)::text, 12, '0'))::uuid,
    0.00000000,
    0.00000000
from generate_series(1, 50) as n;

insert into stock_positions (position_id, trader_id, symbol, quantity, reserved_quantity)
select
    ('10000000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid,
    ('00000000-0000-0000-0000-' || lpad((1000 + n)::text, 12, '0'))::uuid,
    'ACME',
    100000.00000000,
    10000.00000000
from generate_series(1, 50) as n;

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
    ('20000000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid,
    ('00000000-0000-0000-0000-' || lpad((1000 + n)::text, 12, '0'))::uuid,
    'ACME',
    'SELL',
    100.00000000,
    10000.00000000,
    10000.00000000,
    'ACCEPTED',
    timestamp with time zone '2026-01-01 00:00:00+00' + (n || ' milliseconds')::interval
from generate_series(1, 50) as n;

commit;

select
    'baseline seeded' as result,
    100 as buyer_accounts,
    50 as seller_accounts,
    50 as initial_sell_orders,
    10000000000.00000000 as total_cash,
    5000000.00000000 as total_acme_quantity;
