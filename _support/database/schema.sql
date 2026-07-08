create table if not exists trader_accounts (
    trader_id char(36) primary key,
    cash_balance decimal(19, 8) not null,
    reserved_cash decimal(19, 8) not null,
    constraint trader_accounts_cash_balance_non_negative check (cash_balance >= 0),
    constraint trader_accounts_reserved_cash_non_negative check (reserved_cash >= 0),
    constraint trader_accounts_reserved_cash_not_above_balance check (reserved_cash <= cash_balance)
);

create table if not exists stock_positions (
    position_id char(36) primary key,
    trader_id char(36) not null,
    symbol varchar(20) not null,
    quantity decimal(19, 8) not null,
    reserved_quantity decimal(19, 8) not null,
    constraint stock_positions_trader_symbol_unique unique (trader_id, symbol),
    constraint stock_positions_quantity_non_negative check (quantity >= 0),
    constraint stock_positions_reserved_quantity_non_negative check (reserved_quantity >= 0),
    constraint stock_positions_reserved_quantity_not_above_quantity check (reserved_quantity <= quantity),
    constraint stock_positions_trader_accounts_fk foreign key (trader_id) references trader_accounts (trader_id)
);

create table if not exists orders (
    order_id char(36) primary key,
    trader_id char(36) not null,
    symbol varchar(20) not null,
    side varchar(10) not null,
    limit_price decimal(19, 8) not null,
    quantity decimal(19, 8) not null,
    remaining_quantity decimal(19, 8) not null,
    status varchar(20) not null,
    created_at datetime(6) not null,
    constraint orders_side_valid check (side in ('BUY', 'SELL')),
    constraint orders_status_valid check (status in ('ACCEPTED', 'PARTIALLY_FILLED', 'FILLED')),
    constraint orders_limit_price_positive check (limit_price > 0),
    constraint orders_quantity_positive check (quantity > 0),
    constraint orders_remaining_quantity_non_negative check (remaining_quantity >= 0),
    constraint orders_remaining_quantity_not_above_quantity check (remaining_quantity <= quantity),
    constraint orders_trader_accounts_fk foreign key (trader_id) references trader_accounts (trader_id),
    index idx_orders_buy_match (symbol, status, side, limit_price desc, created_at asc),
    index idx_orders_sell_match (symbol, status, side, limit_price asc, created_at asc)
);

create table if not exists trades (
    trade_id char(36) primary key,
    buy_order_id char(36) not null,
    sell_order_id char(36) not null,
    buyer_trader_id char(36) not null,
    seller_trader_id char(36) not null,
    symbol varchar(20) not null,
    price decimal(19, 8) not null,
    quantity decimal(19, 8) not null,
    created_at datetime(6) not null,
    constraint trades_price_positive check (price > 0),
    constraint trades_quantity_positive check (quantity > 0),
    constraint trades_buy_order_fk foreign key (buy_order_id) references orders (order_id),
    constraint trades_sell_order_fk foreign key (sell_order_id) references orders (order_id),
    constraint trades_buyer_trader_account_fk foreign key (buyer_trader_id) references trader_accounts (trader_id),
    constraint trades_seller_trader_account_fk foreign key (seller_trader_id) references trader_accounts (trader_id),
    index idx_trades_symbol_created_at (symbol, created_at desc)
);
