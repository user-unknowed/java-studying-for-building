package com.example.scada.db;

import com.example.scada.model.AlarmEvent;
import com.example.scada.model.DeviceReading;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 监控数据仓库（SQLite 持久化）。
 *
 * <p>复用《Java 与 SQL 数据库 - 深度融合教学》的 JDBC 六步模式：
 * 建连接 → 建表 → PreparedStatement 传参 → executeUpdate → ResultSet 读 → try-with-resources 关闭。
 *
 * <p>连接串参数沿用 order-service 的并发经验：
 * <code>WAL</code>（读写可并行）+ <code>busy_timeout</code>（写冲突时等待而非直接失败）。
 */
public class MonitorDb implements AutoCloseable {

    private final Connection conn;

    public MonitorDb(String dbFile) throws SQLException {
        try {
            Path parent = Path.of(dbFile).toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (Exception ignored) {
        }
        conn = DriverManager.getConnection(
                "jdbc:sqlite:" + dbFile + "?journal_mode=WAL&busy_timeout=5000");
        init();
    }

    private void init() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS readings(
                        id       INTEGER PRIMARY KEY AUTOINCREMENT,
                        ts       TEXT    NOT NULL,
                        temp_c   REAL    NOT NULL,
                        humidity REAL    NOT NULL,
                        light    INTEGER NOT NULL,
                        soil     REAL    NOT NULL,
                        max_c    REAL    NOT NULL,
                        alarm    INTEGER NOT NULL DEFAULT 0
                    )""");
            st.execute("""
                    CREATE TABLE IF NOT EXISTS alarms(
                        id      INTEGER PRIMARY KEY AUTOINCREMENT,
                        ts      TEXT    NOT NULL,
                        kind    TEXT    NOT NULL,
                        message TEXT    NOT NULL,
                        value   REAL    NOT NULL
                    )""");
            st.execute("""
                    CREATE TABLE IF NOT EXISTS ops_log(
                        id     INTEGER PRIMARY KEY AUTOINCREMENT,
                        ts     TEXT NOT NULL,
                        action TEXT NOT NULL,
                        detail TEXT NOT NULL
                    )""");
        }
    }

    public void insertReading(DeviceReading r) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO readings(ts,temp_c,humidity,light,soil,max_c,alarm) VALUES(?,?,?,?,?,?,?)")) {
            ps.setString(1, r.ts().toString());
            ps.setDouble(2, r.tempC());
            ps.setDouble(3, r.humidity());
            ps.setInt(4, r.light());
            ps.setDouble(5, r.soilPct());
            ps.setDouble(6, r.maxTempC());
            ps.setInt(7, r.alarm() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    public void insertAlarm(AlarmEvent a) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO alarms(ts,kind,message,value) VALUES(?,?,?,?)")) {
            ps.setString(1, a.ts().toString());
            ps.setString(2, a.kind());
            ps.setString(3, a.message());
            ps.setDouble(4, a.value());
            ps.executeUpdate();
        }
    }

    public void logOp(String action, String detail) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO ops_log(ts,action,detail) VALUES(?,?,?)")) {
            ps.setString(1, Instant.now().toString());
            ps.setString(2, action);
            ps.setString(3, detail);
            ps.executeUpdate();
        }
    }

    /** 报表：读数总览 + 最近读数 + 报警记录（返回可直接打印的多行文本）。 */
    public String report() throws SQLException {
        var sb = new StringBuilder();
        sb.append("┌─ 采集数据总览 ─────────────────────────────\n");
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT COUNT(*) n, AVG(temp_c) at, MIN(temp_c) lo, MAX(temp_c) hi, SUM(alarm) ac FROM readings")) {
            if (rs.next()) {
                sb.append(String.format("│ 采集轮数 : %d%n", rs.getInt("n")));
                sb.append(String.format("│ 温度均值 : %.1f℃（最低 %.1f / 最高 %.1f）%n",
                        rs.getDouble("at"), rs.getDouble("lo"), rs.getDouble("hi")));
                sb.append(String.format("│ 报警轮数 : %d%n", rs.getInt("ac")));
            }
        }
        sb.append("└────────────────────────────────────────────\n");

        sb.append("最近 5 条读数:\n");
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT ts, temp_c, humidity, light, soil, alarm FROM readings ORDER BY id DESC LIMIT 5")) {
            while (rs.next()) {
                sb.append("  ").append(fmtRow(rs)).append('\n');
            }
        }

        sb.append("报警记录（最近 5 条）:\n");
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT ts, kind, message FROM alarms ORDER BY id DESC LIMIT 5")) {
            boolean any = false;
            while (rs.next()) {
                any = true;
                sb.append("  ").append(shortTs(rs.getString("ts"))).append(" [")
                        .append(rs.getString("kind")).append("] ")
                        .append(rs.getString("message")).append('\n');
            }
            if (!any) {
                sb.append("  (无)\n");
            }
        }
        return sb.toString();
    }

    /** 最近 N 条读数（表格行形式，供 Swing 界面使用）。 */
    public List<String[]> recentReadingRows(int limit) throws SQLException {
        var rows = new ArrayList<String[]>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT ts, temp_c, humidity, light, soil, alarm FROM readings ORDER BY id DESC LIMIT ?")) {
            ps.setInt(1, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{
                            shortTs(rs.getString("ts")),
                            String.format("%.1f", rs.getDouble("temp_c")),
                            String.format("%.1f", rs.getDouble("humidity")),
                            String.valueOf(rs.getInt("light")),
                            String.format("%.1f", rs.getDouble("soil")),
                            rs.getInt("alarm") != 0 ? "⚠报警" : "正常"
                    });
                }
            }
        }
        return rows;
    }

    private static String fmtRow(ResultSet rs) throws SQLException {
        return String.format("%s  temp=%.1f℃ hum=%.0f%% light=%dlx soil=%.0f%% %s",
                shortTs(rs.getString("ts")),
                rs.getDouble("temp_c"), rs.getDouble("humidity"),
                rs.getInt("light"), rs.getDouble("soil"),
                rs.getInt("alarm") != 0 ? "⚠" : "");
    }

    private static String shortTs(String iso) {
        return iso.length() >= 19 ? iso.substring(11, 19) : iso;
    }

    @Override
    public void close() throws SQLException {
        conn.close();
    }
}