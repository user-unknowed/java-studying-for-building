package com.learn.order.repo;

import com.learn.order.model.Product;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** 商品数据访问层（DAO）：只负责「和数据库打交道」 */
@Repository
public class ProductRepository {

    private final JdbcTemplate jdbc;

    // 构造器注入：Spring 自动把容器里的 JdbcTemplate 传进来（依赖注入）
    public ProductRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Product> MAPPER = (rs, i) ->
            new Product(rs.getLong("id"), rs.getString("name"), rs.getLong("price_cents"), rs.getInt("stock"));

    public List<Product> findAll() {
        return jdbc.query("SELECT id, name, price_cents, stock FROM product ORDER BY id", MAPPER);
    }

    public Optional<Product> findById(long id) {
        return jdbc.query("SELECT id, name, price_cents, stock FROM product WHERE id = ?", MAPPER, id)
                .stream().findFirst();
    }

    /**
     * 扣减库存：带「库存充足」条件的原子 UPDATE，返回受影响行数。
     * WHERE 里再带一个 stock >= ? 条件，即使并发请求同时到达，数据库也能保证不会超卖。
     */
    public int decreaseStock(long productId, int quantity) {
        return jdbc.update("UPDATE product SET stock = stock - ? WHERE id = ? AND stock >= ?",
                quantity, productId, quantity);
    }

    /** 新增商品，返回自增主键 */
    public long insert(String name, long priceCents, int stock) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO product(name, price_cents, stock) VALUES (?, ?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setLong(2, priceCents);
            ps.setInt(3, stock);
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }
}