package com.demo.library;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/** 数据库连接与初始化工具 */
public final class Db {

    /**
     * SQLite 连接串：
     *   journal_mode=WAL   提升并发读性能（写不阻塞读）
     *   busy_timeout=5000  写锁被占用时等待最多 5 秒（而不是立刻报错）
     */
    private static final String URL =
            "jdbc:sqlite:library.db?journal_mode=WAL&busy_timeout=5000";

    private Db() {}

    public static Connection get() throws Exception {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection(URL);
    }

    /** 建表（幂等：IF NOT EXISTS） */
    public static void init() throws Exception {
        try (Connection c = get(); Statement st = c.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS books (
                        id     INTEGER PRIMARY KEY AUTOINCREMENT,
                        title  TEXT    NOT NULL,
                        author TEXT    NOT NULL,
                        stock  INTEGER NOT NULL DEFAULT 0
                    )""");
            st.execute("""
                    CREATE TABLE IF NOT EXISTS borrow_records (
                        id          INTEGER PRIMARY KEY AUTOINCREMENT,
                        book_id     INTEGER NOT NULL,
                        borrower    TEXT    NOT NULL,
                        borrowed_at TEXT    NOT NULL DEFAULT (datetime('now', 'localtime')),
                        returned_at TEXT
                    )""");
            System.out.println("[init] 数据库就绪: books / borrow_records");
        }
    }
}
