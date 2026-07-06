set foreign_key_checks = 0;
truncate table trades;
truncate table orders;
truncate table stock_positions;
truncate table trader_accounts;
set foreign_key_checks = 1;

insert into trader_accounts (trader_id, cash_balance, reserved_cash)
with recursive numbers(n) as (
    select 1
    union all
    select n + 1 from numbers where n < 100
)
select
    concat('00000000-0000-0000-0000-', lpad(cast(n as char), 12, '0')),
    100000000.00000000,
    0.00000000
from numbers;

insert into trader_accounts (trader_id, cash_balance, reserved_cash)
with recursive numbers(n) as (
    select 1
    union all
    select n + 1 from numbers where n < 50
)
select
    concat('00000000-0000-0000-0000-', lpad(cast(1000 + n as char), 12, '0')),
    0.00000000,
    0.00000000
from numbers;

insert into stock_positions (position_id, trader_id, symbol, quantity, reserved_quantity)
with recursive numbers(n) as (
    select 1
    union all
    select n + 1 from numbers where n < 50
)
select
    concat('10000000-0000-0000-0000-', lpad(cast(n as char), 12, '0')),
    concat('00000000-0000-0000-0000-', lpad(cast(1000 + n as char), 12, '0')),
    'ACME',
    100000.00000000,
    10000.00000000
from numbers;

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
with recursive numbers(n) as (
    select 1
    union all
    select n + 1 from numbers where n < 50
)
select
    concat('20000000-0000-0000-0000-', lpad(cast(n as char), 12, '0')),
    concat('00000000-0000-0000-0000-', lpad(cast(1000 + n as char), 12, '0')),
    'ACME',
    'SELL',
    100.00000000,
    10000.00000000,
    10000.00000000,
    'ACCEPTED',
    date_add('2026-01-01 00:00:00.000000', interval n microsecond)
from numbers;

select
    'baseline seeded' as result,
    100 as buyer_accounts,
    50 as seller_accounts,
    50 as initial_sell_orders,
    10000000000.00000000 as total_cash,
    5000000.00000000 as total_acme_quantity;
