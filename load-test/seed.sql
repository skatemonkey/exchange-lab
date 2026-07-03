begin;

truncate table trades, orders, stock_positions, trader_accounts;

insert into trader_accounts (trader_id, cash_balance, reserved_cash)
select
    ('00000000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid,
    100000.00000000,
    0.00000000
from generate_series(1, 100) as n;

insert into trader_accounts (trader_id, cash_balance, reserved_cash)
select
    ('00000000-0000-0000-0000-' || lpad((1000 + n)::text, 12, '0'))::uuid,
    0.00000000,
    0.00000000
from generate_series(1, 100) as n;

insert into stock_positions (position_id, trader_id, symbol, quantity, reserved_quantity)
select
    ('10000000-0000-0000-0000-' || lpad(n::text, 12, '0'))::uuid,
    ('00000000-0000-0000-0000-' || lpad((1000 + n)::text, 12, '0'))::uuid,
    'ACME',
    1000.00000000,
    0.00000000
from generate_series(1, 100) as n;

commit;

select
    'baseline seeded' as result,
    100 as buyer_accounts,
    100 as seller_accounts,
    10000000.00000000 as total_cash,
    100000.00000000 as total_acme_quantity;
