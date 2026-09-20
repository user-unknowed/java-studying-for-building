package com.demo.order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 商品数据访问 */
public class ProductDao {

    public List<Map<String, Object>> list() throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection c = Db.get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT id, name, price, stock FROM products ORDER BY id")) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Map<String, Object> get(long id) throws Exception {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, name, price, stock FROM products WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private Map<String, Object> map(ResultSet rs) throws SQLException {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", rs.getLong("id"));
        m.put("name", rs.getString("name"));
        m.put("price", rs.getDouble("price"));
        m.put("stock", rs.getInt("stock"));
        return m;
    }
}