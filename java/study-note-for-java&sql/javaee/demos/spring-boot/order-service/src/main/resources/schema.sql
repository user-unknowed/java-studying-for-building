-- 电商订单服务 · 建表脚本
-- 金额一律用「分」(INTEGER) 存储

CREATE TABLE IF NOT EXISTS product (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL UNIQUE,
    price_cents INTEGER NOT NULL CHECK (price_cents >= 0),
    stock       INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0)
);

CREATE TABLE IF NOT EXISTS orders (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_name   TEXT    NOT NULL,
    total_cents INTEGER NOT NULL,
    status      TEXT    NOT NULL DEFAULT 'PAID',
    created_at  TEXT    NOT NULL DEFAULT (datetime('now', 'localtime'))
);

CREATE TABLE IF NOT EXISTS order_item (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id     INTEGER NOT NULL REFERENCES orders(id),
    product_id   INTEGER NOT NULL,
    product_name TEXT    NOT NULL,
    price_cents  INTEGER NOT NULL,
    quantity     INTEGER NOT NULL
);

-- 查询订单明细的常用索引
CREATE INDEX IF NOT EXISTS idx_order_item_order ON order_item(order_id);
