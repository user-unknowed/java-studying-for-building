package com.demo.order;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计接口：
 *   GET /api/stats/sales    每商品销量（已取消订单剔除）
 *   GET /api/stats/summary  订单概况（单数 / 成交额）
 */
public class StatsApiServlet extends HttpServlet {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo();
        try {
            if (path == null || path.equals("/") || path.equals("/sales")) {
                write(resp, 200, sales());
            } else if (path.equals("/summary")) {
                write(resp, 200, summary());
            } else {
                write(resp, 404, Map.of("error", "未知统计路径: " + path));
            }
        } catch (Exception e) {
            write(resp, 500, Map.of("error", e.getMessage()));
        }
    }

    /** 每商品累计销量：LEFT JOIN + CASE WHEN 剔除已取消订单 */
    private List<Map<String, Object>> sales() throws Exception {
        String sql = """
                SELECT p.id, p.name, p.price, p.stock,
                       COALESCE(SUM(CASE WHEN o.status = 'CREATED' THEN oi.quantity ELSE 0 END), 0) AS sold
                FROM products p
                LEFT JOIN order_items oi ON oi.product_id = p.id
                LEFT JOIN orders o ON o.id = oi.order_id
                GROUP BY p.id
                ORDER BY sold DESC, p.id""";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection c = Db.get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", rs.getLong("id"));
                m.put("name", rs.getString("name"));
                m.put("price", rs.getDouble("price"));
                m.put("stock", rs.getInt("stock"));
                m.put("sold", rs.getInt("sold"));
                list.add(m);
            }
        }
        return list;
    }

    /** 订单概况 */
    private Map<String, Object> summary() throws Exception {
        Map<String, Object> m = new LinkedHashMap<>();
        try (Connection c = Db.get();
             Statement st = c.createStatement()) {
            try (ResultSet rs = st.executeQuery("""
                    SELECT COUNT(*) AS cnt, COALESCE(SUM(total), 0) AS amount
                    FROM orders WHERE status = 'CREATED'""")) {
                rs.next();
                m.put("createdOrderCount", rs.getInt("cnt"));
                m.put("totalAmount", rs.getDouble("amount"));
            }
            try (ResultSet rs = st.executeQuery(
                    "SELECT COUNT(*) FROM orders WHERE status = 'CANCELED'")) {
                rs.next();
                m.put("canceledOrderCount", rs.getInt(1));
            }
        }
        return m;
    }

    private void write(HttpServletResponse resp, int code, Object data) throws IOException {
        resp.setStatus(code);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(GSON.toJson(data));
    }
}