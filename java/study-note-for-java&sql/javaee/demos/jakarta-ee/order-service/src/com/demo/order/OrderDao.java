package com.demo.order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 订单数据访问（含核心事务逻辑） */
public class OrderDao {

    /** 业务异常：携带 HTTP 状态码，交给 Servlet 转为响应 */
    public static class BizException extends RuntimeException {
        public final int httpCode;

        public BizException(int httpCode, String message) {
            super(message);
            this.httpCode = httpCode;
        }
    }

    /** 下单请求中的一行商品 */
    public static class Item {
        public long productId;
        public int quantity;
    }

    /**
     * 下单（单个数据库事务）：
     *   ① 对每个商品做条件扣减：UPDATE ... SET stock = stock - ? WHERE id = ? AND stock >= ?
     *      —— "库存足够才扣成功"，并发下不会超卖
     *   ② 创建订单主记录（金额 = 各商品单价 × 数量之和）
     *   ③ 写入订单明细
     * 任何一步失败 → rollback → 数据零变化
     */
    public Map<String, Object> place(String userId, List<Item> items) throws Exception {
        if (items == null || items.isEmpty()) {
            throw new BizException(400, "items 不能为空");
        }
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                double total = 0;
                Map<Long, Double> priceCache = new HashMap<>(); // 缓存单价，写明细时复用
                for (Item it : items) {
                    if (it.quantity <= 0) {
                        throw new BizException(400, "quantity 必须为正整数");
                    }

                    // ① 条件扣减库存（原子操作，防超卖的关键）
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?")) {
                        ps.setInt(1, it.quantity);
                        ps.setLong(2, it.productId);
                        ps.setInt(3, it.quantity);
                        if (ps.executeUpdate() == 0) {
                            boolean exists;
                            try (PreparedStatement q = c.prepareStatement("SELECT 1 FROM products WHERE id = ?")) {
                                q.setLong(1, it.productId);
                                try (ResultSet rs = q.executeQuery()) {
                                    exists = rs.next();
                                }
                            }
                            c.rollback();
                            throw new BizException(
                                    exists ? 409 : 404,
                                    exists ? "库存不足: productId=" + it.productId
                                           : "商品不存在: productId=" + it.productId);
                        }
                    }

                    // 读取单价（同一事务内，数据一致）
                    try (PreparedStatement ps = c.prepareStatement(
                            "SELECT price FROM products WHERE id = ?")) {
                        ps.setLong(1, it.productId);
                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            double price = rs.getDouble(1);
                            priceCache.put(it.productId, price);
                            total += price * it.quantity;
                        }
                    }
                }

                // ② 创建订单
                long orderId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO orders(user_id, total, status) VALUES(?, ?, 'CREATED')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, userId);
                    ps.setDouble(2, total);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        orderId = keys.getLong(1);
                    }
                }

                // ③ 写订单明细
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO order_items(order_id, product_id, quantity, price) VALUES(?, ?, ?, ?)")) {
                    for (Item it : items) {
                        ps.setLong(1, orderId);
                        ps.setLong(2, it.productId);
                        ps.setInt(3, it.quantity);
                        ps.setDouble(4, priceCache.get(it.productId));
                        ps.executeUpdate();
                    }
                }

                c.commit();

                Map<String, Object> result = new LinkedHashMap<>();
                result.put("orderId", orderId);
                result.put("status", "CREATED");
                result.put("total", total);
                return result;
            } catch (BizException e) {
                c.rollback();
                throw e;
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** 订单详情（含明细，JOIN 商品名） */
    public Map<String, Object> get(long orderId) throws Exception {
        try (Connection c = Db.get()) {
            Map<String, Object> order = null;
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT id, user_id, total, status, created_at FROM orders WHERE id = ?")) {
                ps.setLong(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        order = new LinkedHashMap<>();
                        order.put("id", rs.getLong("id"));
                        order.put("userId", rs.getString("user_id"));
                        order.put("total", rs.getDouble("total"));
                        order.put("status", rs.getString("status"));
                        order.put("createdAt", rs.getString("created_at"));
                    }
                }
            }
            if (order == null) {
                return null;
            }

            List<Map<String, Object>> items = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement("""
                    SELECT oi.product_id, p.name, oi.quantity, oi.price
                    FROM order_items oi
                    JOIN products p ON p.id = oi.product_id
                    WHERE oi.order_id = ?
                    ORDER BY oi.id""")) {
                ps.setLong(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("productId", rs.getLong("product_id"));
                        m.put("name", rs.getString("name"));
                        m.put("quantity", rs.getInt("quantity"));
                        m.put("price", rs.getDouble("price"));
                        items.add(m);
                    }
                }
            }
            order.put("items", items);
            return order;
        }
    }

    /**
     * 取消订单（事务）：
     *   ① 只有 CREATED 状态可取消（条件更新，防重复取消导致库存翻倍回滚）
     *   ② 库存回滚
     * @return 取消后的订单详情
     */
    public Map<String, Object> cancel(long orderId) throws Exception {
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                // ① 状态守卫：只有 CREATED → CANCELED 的这一次更新能成功
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE orders SET status = 'CANCELED' WHERE id = ? AND status = 'CREATED'")) {
                    ps.setLong(1, orderId);
                    if (ps.executeUpdate() == 0) {
                        boolean exists;
                        try (PreparedStatement q = c.prepareStatement("SELECT 1 FROM orders WHERE id = ?")) {
                            q.setLong(1, orderId);
                            try (ResultSet rs = q.executeQuery()) {
                                exists = rs.next();
                            }
                        }
                        c.rollback();
                        throw new BizException(exists ? 409 : 404,
                                exists ? "订单当前状态不可取消（可能已取消）" : "订单不存在: id=" + orderId);
                    }
                }

                // ② 库存回滚：把订单中每个商品的购买数量加回库存
                List<Item> items = new ArrayList<>();
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT product_id, quantity FROM order_items WHERE order_id = ?")) {
                    ps.setLong(1, orderId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            Item it = new Item();
                            it.productId = rs.getLong("product_id");
                            it.quantity = rs.getInt("quantity");
                            items.add(it);
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE products SET stock = stock + ? WHERE id = ?")) {
                    for (Item it : items) {
                        ps.setInt(1, it.quantity);
                        ps.setLong(2, it.productId);
                        ps.executeUpdate();
                    }
                }

                c.commit();
            } catch (BizException e) {
                c.rollback();
                throw e;
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
        return get(orderId);
    }
}