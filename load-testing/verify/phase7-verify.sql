select 'table counts' as check_group;

select 'trader_accounts' as table_name, count(*) as row_count from trader_accounts
union all
select 'stock_positions', count(*) from stock_positions
union all
select 'orders', count(*) from orders
union all
select 'trades', count(*) from trades
order by table_name;

select 'cash conservation' as check_group;

select
    sum(cash_balance) as total_cash,
    sum(reserved_cash) as total_reserved_cash,
    100000000000.00000000 as expected_total_cash,
    sum(cash_balance) = 100000000000.00000000 as cash_conserved
from trader_accounts;

select 'stock conservation' as check_group;

select
    symbol,
    sum(quantity) as total_quantity,
    sum(reserved_quantity) as total_reserved_quantity,
    50000000.00000000 as expected_total_quantity,
    sum(quantity) = 50000000.00000000 as stock_conserved
from stock_positions
group by symbol;

select 'order and trade summary' as check_group;

select
    side,
    status,
    count(*) as order_count,
    sum(quantity) as total_quantity,
    sum(remaining_quantity) as total_remaining_quantity
from orders
group by side, status
order by side, status;

select
    count(*) as trade_count,
    coalesce(sum(quantity), 0) as traded_quantity,
    coalesce(sum(price * quantity), 0) as traded_value
from trades;

select 'trade quantity relation' as check_group;

select
    (
        select coalesce(sum(quantity - remaining_quantity), 0)
        from orders
        where side = 'BUY'
    ) as filled_buy_quantity,
    (
        select coalesce(sum(quantity), 0)
        from trades
    ) as traded_quantity,
    (
        select coalesce(sum(quantity - remaining_quantity), 0)
        from orders
        where side = 'BUY'
    ) = (
        select coalesce(sum(quantity), 0)
        from trades
    ) as quantities_match;

select 'invalid state checks' as check_group;

select 'negative trader cash balance' as check_name, count(*) as bad_rows
from trader_accounts
where cash_balance < 0
union all
select 'negative reserved cash', count(*)
from trader_accounts
where reserved_cash < 0
union all
select 'reserved cash above balance', count(*)
from trader_accounts
where reserved_cash > cash_balance
union all
select 'negative stock quantity', count(*)
from stock_positions
where quantity < 0
union all
select 'negative reserved stock', count(*)
from stock_positions
where reserved_quantity < 0
union all
select 'reserved stock above quantity', count(*)
from stock_positions
where reserved_quantity > quantity
union all
select 'negative order remaining quantity', count(*)
from orders
where remaining_quantity < 0
union all
select 'order remaining above original quantity', count(*)
from orders
where remaining_quantity > quantity
order by check_name;
