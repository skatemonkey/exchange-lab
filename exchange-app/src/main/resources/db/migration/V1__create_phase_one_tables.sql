create table trader_accounts (
    trader_id uuid primary key,
    cash_balance numeric(19, 8) not null,
    reserved_cash numeric(19, 8) not null,
    constraint trader_accounts_cash_balance_non_negative check (cash_balance >= 0),
    constraint trader_accounts_reserved_cash_non_negative check (reserved_cash >= 0),
    constraint trader_accounts_reserved_cash_not_above_balance check (reserved_cash <= cash_balance)
);

create table stock_positions (
    position_id uuid primary key,
    trader_id uuid not null references trader_accounts (trader_id),
    symbol varchar(20) not null,
    quantity numeric(19, 8) not null,
    reserved_quantity numeric(19, 8) not null,
    constraint stock_positions_trader_symbol_unique unique (trader_id, symbol),
    constraint stock_positions_quantity_non_negative check (quantity >= 0),
    constraint stock_positions_reserved_quantity_non_negative check (reserved_quantity >= 0),
    constraint stock_positions_reserved_quantity_not_above_quantity check (reserved_quantity <= quantity)
);

create table orders (
    order_id uuid primary key,
    trader_id uuid not null references trader_accounts (trader_id),
    symbol varchar(20) not null,
    side varchar(10) not null,
    limit_price numeric(19, 8) not null,
    quantity numeric(19, 8) not null,
    remaining_quantity numeric(19, 8) not null,
    status varchar(20) not null,
    created_at timestamp(6) with time zone not null,
    constraint orders_side_valid check (side in ('BUY', 'SELL')),
    constraint orders_status_valid check (status in ('ACCEPTED', 'PARTIALLY_FILLED', 'FILLED')),
    constraint orders_limit_price_positive check (limit_price > 0),
    constraint orders_quantity_positive check (quantity > 0),
    constraint orders_remaining_quantity_non_negative check (remaining_quantity >= 0),
    constraint orders_remaining_quantity_not_above_quantity check (remaining_quantity <= quantity)
);

create index idx_orders_buy_match
    on orders (symbol, status, side, limit_price desc, created_at asc);

create index idx_orders_sell_match
    on orders (symbol, status, side, limit_price asc, created_at asc);

create table trades (
    trade_id uuid primary key,
    buy_order_id uuid not null references orders (order_id),
    sell_order_id uuid not null references orders (order_id),
    buyer_trader_id uuid not null references trader_accounts (trader_id),
    seller_trader_id uuid not null references trader_accounts (trader_id),
    symbol varchar(20) not null,
    price numeric(19, 8) not null,
    quantity numeric(19, 8) not null,
    created_at timestamp(6) with time zone not null,
    constraint trades_price_positive check (price > 0),
    constraint trades_quantity_positive check (quantity > 0)
);

create index idx_trades_symbol_created_at
    on trades (symbol, created_at desc);
