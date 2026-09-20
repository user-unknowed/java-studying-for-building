package com.demo.library;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 图书数据访问层（DAO）：所有 SQL 集中在这里，Servlet 不碰数据库细节 */
public class BookDao {

    private Book map(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.id = rs.getLong("id");
        b.title = rs.getString("title");
        b.author = rs.getString("author");
        b.stock = rs.getInt("stock");
        return b;
    }

    /** 全部图书 */
    public List<Book> list() throws Exception {
        List<Book> books = new ArrayList<>();
        try (Connection c = Db.get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, title, author, stock FROM books ORDER BY id")) {
            while (rs.next()) {
                books.add(map(rs));
            }
        }
        return books;
    }

    /** 单本图书；不存在返回 null */
    public Book get(long id) throws Exception {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, title, author, stock FROM books WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** 新增图书 */
    public Book create(String title, String author, int stock) throws Exception {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO books(title, author, stock) VALUES(?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setInt(3, stock);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new Book(keys.getLong(1), title, author, stock);
            }
        }
    }

    /**
     * 借书（事务）：① 条件扣库存（stock > 0 才能扣） ② 写入借阅记录
     *
     * @return OK / NO_STOCK / NOT_FOUND
     */
    public String borrow(long bookId, String borrower) throws Exception {
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE books SET stock = stock - 1 WHERE id = ? AND stock > 0")) {
                    ps.setLong(1, bookId);
                    if (ps.executeUpdate() == 0) {
                        // 扣不动：要么书不存在，要么没库存 —— 区分一下给调用方准确的原因
                        boolean exists;
                        try (PreparedStatement q = c.prepareStatement("SELECT 1 FROM books WHERE id = ?")) {
                            q.setLong(1, bookId);
                            try (ResultSet rs = q.executeQuery()) {
                                exists = rs.next();
                            }
                        }
                        c.rollback();
                        return exists ? "NO_STOCK" : "NOT_FOUND";
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO borrow_records(book_id, borrower) VALUES(?, ?)")) {
                    ps.setLong(1, bookId);
                    ps.setString(2, borrower);
                    ps.executeUpdate();
                }
                c.commit();
                return "OK";
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    /**
     * 还书（事务）：① 找到该借阅人未归还的记录并标记归还时间 ② 库存 +1
     *
     * @return OK / NO_RECORD
     */
    public String giveBack(long bookId, String borrower) throws Exception {
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                long recordId;
                try (PreparedStatement ps = c.prepareStatement("""
                        SELECT id FROM borrow_records
                        WHERE book_id = ? AND borrower = ? AND returned_at IS NULL
                        ORDER BY id LIMIT 1""")) {
                    ps.setLong(1, bookId);
                    ps.setString(2, borrower);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            c.rollback();
                            return "NO_RECORD";
                        }
                        recordId = rs.getLong(1);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE borrow_records SET returned_at = datetime('now', 'localtime') WHERE id = ?")) {
                    ps.setLong(1, recordId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE books SET stock = stock + 1 WHERE id = ?")) {
                    ps.setLong(1, bookId);
                    ps.executeUpdate();
                }
                c.commit();
                return "OK";
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** 借阅排行榜：LEFT JOIN + GROUP BY 聚合统计 */
    public List<Map<String, Object>> popular() throws Exception {
        String sql = """
                SELECT b.id, b.title, COUNT(r.id) AS borrow_count
                FROM books b
                LEFT JOIN borrow_records r ON r.book_id = b.id
                GROUP BY b.id, b.title
                ORDER BY borrow_count DESC, b.id""";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection c = Db.get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", rs.getLong("id"));
                m.put("title", rs.getString("title"));
                m.put("borrowCount", rs.getInt("borrow_count"));
                list.add(m);
            }
        }
        return list;
    }
}
