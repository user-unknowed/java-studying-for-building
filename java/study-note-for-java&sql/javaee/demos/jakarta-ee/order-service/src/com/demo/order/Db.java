package com.demo.order;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/** 数据库连接与初始化（含种子数据） */
public final class Db {

    private static final String URL =
            "jdbc:sqlite:order.db?journal_mode=WAL&busy_timeout=5000";

    private Db() {}

    public static Connection get() throws Exception {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection(URL);
    }

    public static void init() throws Exception {
        try (Connection c = get(); Statement st = c.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS products (
                        id    INTEGER PRIMARY KEY AUTOINCREMENT,
                        name  TEXT    NOT NULL,
                        price REAL    NOT NULL,
                        stock INTEGER NOT NULL DEFAULT 0
                    )""");
            st.execute("""
                    CREATE TABLE IF NOT EXISTS orders (
                        id         INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id    TEXT    NOT NULL,
                        total      REAL    NOT NULL,
                        status     TEXT    NOT NULL DEFAULT 'CREATED',
                        created_at TEXT    NOT NULL DEFAULT (datetime('now', 'localtime'))
                    )""");
            st.execute("""
                    CREATE TABLE IF NOT EXISTS order_items (
                        id         INTEGER PRIMARY KEY AUTOINCREMENT,
                        order_id   INTEGER NOT NULL,
                        product_id INTEGER NOT NULL,
                        quantity   INTEGER NOT NULL,
                        price      REAL    NOT NULL
                    )""");

            // 种子数据：仅当商品表为空时插入，保证多次启动幂等
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM products")) {
                rs.next();
                if (rs.getInt(1) == 0) {
                    st.execute("INSERT INTO products(name, price, stock) VALUES ('机械键盘', 399.00, 5)");
                    st.execute("INSERT INTO products(name, price, stock) VALUES ('游戏鼠标', 199.00, 100)");
                    st.execute("INSERT INTO products(name, price, stock) VALUES ('4K显示器', 1299.00, 3)");
                    System.out.println("[init] 已插入 3 件示例商品");
                }
            }
            System.out.println("[init] 数据库就绪: products / orders / order_items");
        }
    }
}