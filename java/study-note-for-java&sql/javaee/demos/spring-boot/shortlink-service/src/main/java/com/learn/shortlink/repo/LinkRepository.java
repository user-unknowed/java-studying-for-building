package com.learn.shortlink.repo;

import com.learn.shortlink.model.Link;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** 短链数据访问层（DAO）：SQL 只出现在这里 */
@Repository
public class LinkRepository {

    private final JdbcTemplate jdbc;

    public LinkRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Link> MAPPER = (rs, i) ->
            new Link(rs.getLong("id"), rs.getString("code"), rs.getString("original_url"),
                    rs.getLong("clicks"), rs.getString("created_at"));

    /** 短码是否已存在 */
    public boolean existsByCode(String code) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM link WHERE code = ?", Integer.class, code);
        return n != null && n > 0;
    }

    /** 插入短链，返回自增主键 */
    public long insert(String code, String originalUrl) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO link(code, original_url) VALUES (?, ?)",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, code);
            ps.setString(2, originalUrl);
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    public Optional<Link> findByCode(String code) {
        return jdbc.query("SELECT id, code, original_url, clicks, created_at FROM link WHERE code = ?",
                MAPPER, code).stream().findFirst();
    }

    /**
     * 点击计数 +1：单条 UPDATE 在数据库内部原子执行。
     *
     * <p>对比「先读出 clicks 再写回 clicks+1」的读-改-写方案：
     * 并发下两个请求可能读到同一个旧值，各自 +1 后写回，计数就丢了
     * （经典并发问题：丢失更新 / lost update）。</p>
     */
    public void incrementClicks(String code) {
        jdbc.update("UPDATE link SET clicks = clicks + 1 WHERE code = ?", code);
    }

    /** 最近创建的短链（管理视角列表） */
    public List<Link> findRecent(int limit) {
        return jdbc.query("SELECT id, code, original_url, clicks, created_at FROM link ORDER BY id DESC LIMIT ?",
                MAPPER, limit);
    }
}