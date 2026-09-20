-- 初始商品数据（INSERT OR IGNORE：重复启动不会重复插入）
-- 39900 分 = 399.00 元
INSERT OR IGNORE INTO product(name, price_cents, stock) VALUES ('机械键盘',   39900, 50);
INSERT OR IGNORE INTO product(name, price_cents, stock) VALUES ('无线鼠标',   12900, 100);
INSERT OR IGNORE INTO product(name, price_cents, stock) VALUES ('4K显示器',  149900, 20);
INSERT OR IGNORE INTO product(name, price_cents, stock) VALUES ('USB-C扩展坞', 24900, 0);
