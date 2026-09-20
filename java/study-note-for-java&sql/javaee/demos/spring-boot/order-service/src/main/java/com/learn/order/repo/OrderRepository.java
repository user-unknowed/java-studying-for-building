package com.learn.order.repo;

import com.learn.order.model.Order;
import com.learn.order.model.OrderItem;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** 订单数据访问层 */
@Repository
public class OrderRepository {

    private final JdbcTemplate jdbc;

    public OrderRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Order> MAPPER = (rs, i) ->
            new Order(rs.getLong("id"), rs.getString("user_name"), rs.getLong("total_cents"),
                    rs.getString("status"), rs.getString("created_at"));

    private static final RowMapper<OrderItem> ITEM_MAPPER = (rs, i) ->
            new OrderItem(rs.getLong("id"), rs.getLong("order_id"), rs.getLong("product_id"),
                    rs.getString("product_name"), rs.getLong("price_cents"), rs.getInt("quantity"));

    public long insertOrder(String userName, long totalCents) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO orders(user_name, total_cents, status) VALUES (?, ?, 'PAID')",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, userName);
            ps.setLong(2, totalCents);
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public void insertItem(long orderId, OrderItem item) {
        jdbc.update("INSERT INTO order_item(order_id, product_id, product_name, price_cents, quantity) "
                        + "VALUES (?, ?, ?, ?, ?)",
                orderId, item.productId(), item.productName(), item.priceCents(), item.quantity());
    }

    public Optional<Order> findById(long id) {
        return jdbc.query("SELECT id, user_name, total_cents, status, created_at FROM orders WHERE id = ?",
                        MAPPER, id)
                .stream().findFirst();
    }

    public List<OrderItem> findItems(long orderId) {
        return jdbc.query("SELECT id, order_id, product_id, product_name, price_cents, quantity "
                        + "FROM order_item WHERE order_id = ? ORDER BY id",
                ITEM_MAPPER, orderId);
    }

    /** 销售排行：JOIN + GROUP BY 聚合（真实工业报表 SQL） */
    public List<Map<String, Object>> salesByProduct() {
        return jdbc.queryForList("""
                SELECT oi.product_name       AS product,
                       SUM(oi.quantity)      AS sold,
                       SUM(oi.quantity * oi.price_cents) AS revenueCents
                FROM order_item oi
                JOIN orders o ON o.id = oi.order_id
                WHERE o.status = 'PAID'
                GROUP BY oi.product_name
                ORDER BY revenueCents DESC""");
    }

    /** 每日销售额：SQL 日期函数 + 聚合 */
    public List<Map<String, Object>> dailySales() {
        return jdbc.queryForList("""
                SELECT date(o.created_at) AS day,
                       COUNT(*)           AS orders,
                       SUM(o.total_cents) AS revenueCents
                FROM orders o
                GROUP BY date(o.created_at)
                ORDER BY day DESC
                LIMIT 7""");
    }
}