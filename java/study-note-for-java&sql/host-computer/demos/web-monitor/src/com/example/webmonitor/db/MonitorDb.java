package com.example.webmonitor.db;

import com.example.webmonitor.model.Reading;

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
import java.util.Collections;
import java.util.List;

/**
 * Web 版监控数据仓库（SQLite 持久化）。
 *
 * <p>复用《Java 与 SQL 数据库 - 深度融合教学》的 JDBC 六步模式；
 * 连接串沿用 order-service 的并发经验：<code>WAL</code> + <code>busy_timeout</code>。
 *
 * <p>三张表与 06 号教程保持一致（readings / alarms / ops_log），
 * 但为 Web 场景补上了"查询最近 N 条"的接口（图表数据源）。
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

    public void insertReading(Reading r) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO readings(ts,temp_c,humidity,light,soil,max_c,alarm) VALUES(?,?,?,?,?,?,?)")) {
            ps.setString(1, r.ts().toString());
            ps.setDouble(2, r.temp());
            ps.setDouble(3, r.humidity());
            ps.setInt(4, r.light());
            ps.setDouble(5, r.soil());
            ps.setDouble(6, r.limit());
            ps.setInt(7, r.alarm() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    public void insertAlarm(String kind, String message, double value) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO alarms(ts,kind,message,value) VALUES(?,?,?,?)")) {
            ps.setString(1, Instant.now().toString());
            ps.setString(2, kind);
            ps.setString(3, message);
            ps.setDouble(4, value);
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

    /** 最近 N 条读数（时间正序返回 —— 折线图从左到右即时间轴）。 */
    public List<Reading> recentReadings(int limit) throws SQLException {
        var list = new ArrayList<Reading>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT ts,temp_c,humidity,light,soil,max_c,alarm FROM readings ORDER BY id DESC LIMIT ?")) {
            ps.setInt(1, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Reading(
                            Instant.parse(rs.getString("ts")),
                            rs.getDouble("temp_c"),
                            rs.getDouble("humidity"),
                            rs.getInt("light"),
                            rs.getDouble("soil"),
                            rs.getDouble("max_c"),
                            rs.getInt("alarm") != 0));
                }
            }
        }
        Collections.reverse(list);
        return list;
    }

    /** 报警行（给 API/前端用的小视图对象）。 */
    public record AlarmRow(String time, String kind, String message) {
    }

    public List<AlarmRow> recentAlarms(int limit) throws SQLException {
        var list = new ArrayList<AlarmRow>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT ts,kind,message FROM alarms ORDER BY id DESC LIMIT ?")) {
            ps.setInt(1, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AlarmRow(
                            shortTs(rs.getString("ts")),
                            rs.getString("kind"),
                            rs.getString("message")));
                }
            }
        }
        return list;
    }

    public long countReadings() throws SQLException {
        return count("readings");
    }

    public long countAlarms() throws SQLException {
        return count("alarms");
    }

    private long count(String table) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    private static String shortTs(String iso) {
        return iso.length() >= 19 ? iso.substring(11, 19) : iso;
    }

    @Override
    public void close() throws SQLException {
        conn.close();
    }
}