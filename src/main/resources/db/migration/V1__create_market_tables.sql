create table stocks (
    symbol varchar(20) primary key,
    name varchar(255) not null,
    description varchar(255)
);

create table trader_accounts (
    trader_id uuid primary key,
    username varchar(255) not null unique,
    cash_balance numeric(19, 8) not null,
    reserved_cash numeric(19, 8) not null
);

create table positions (
    trader_id uuid not null,
    stock_symbol varchar(20) not null,
    quantity numeric(19, 8) not null,
    reserved_quantity numeric(19, 8) not null,
    primary key (trader_id, stock_symbol)
);

create table orders (
    order_id uuid primary key,
    trader_id uuid not null,
    stock_symbol varchar(20) not null,
    side varchar(10) not null,
    limit_price numeric(19, 8) not null,
    quantity numeric(19, 8) not null,
    remaining_quantity numeric(19, 8) not null,
    status varchar(20) not null,
    submitted_at timestamp(6) with time zone not null
);

create table trades (
    trade_id uuid primary key,
    buy_order_id uuid not null,
    sell_order_id uuid not null,
    buyer_trader_id uuid not null,
    seller_trader_id uuid not null,
    stock_symbol varchar(20) not null,
    price numeric(19, 8) not null,
    quantity numeric(19, 8) not null,
    executed_at timestamp(6) with time zone not null
);

create table account_transactions (
    transaction_id uuid primary key,
    trader_id uuid not null,
    type varchar(30) not null,
    amount numeric(19, 8) not null,
    balance_after numeric(19, 8) not null,
    related_trade_id uuid,
    created_at timestamp(6) with time zone not null
);
