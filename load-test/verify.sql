\pset pager off

select
    'total_cash_balance' as check_name,
    '10000000.00000000' as expected,
    coalesce(sum(cash_balance), 0)::text as actual,
    coalesce(sum(cash_balance), 0) = 10000000.00000000 as pass
from trader_accounts
union all
select
    'total_acme_quantity',
    '100000.00000000',
    coalesce(sum(quantity), 0)::text,
    coalesce(sum(quantity), 0) = 100000.00000000
from stock_positions
where symbol = 'ACME'
union all
select
    'invalid_cash_rows',
    '0',
    count(*)::text,
    count(*) = 0
from trader_accounts
where cash_balance < 0
   or reserved_cash < 0
   or reserved_cash > cash_balance
union all
select
    'invalid_stock_rows',
    '0',
    count(*)::text,
    count(*) = 0
from stock_positions
where quantity < 0
   or reserved_quantity < 0
   or reserved_quantity > quantity
union all
select
    'invalid_order_rows',
    '0',
    count(*)::text,
    count(*) = 0
from orders
where remaining_quantity < 0
   or remaining_quantity > quantity
   or (status = 'FILLED' and remaining_quantity <> 0)
   or (status in ('ACCEPTED', 'PARTIALLY_FILLED') and remaining_quantity = 0);

select status, count(*) as order_count
from orders
group by status
order by status;

select side, count(*) as order_count
from orders
group by side
order by side;

select
    count(*) as trade_count,
    coalesce(sum(quantity), 0) as traded_quantity,
    coalesce(sum(price * quantity), 0) as traded_value
from trades;
