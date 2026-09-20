package com.example.webmonitor.web;

import com.example.webmonitor.db.MonitorDb;
import com.example.webmonitor.service.Poller;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * API 层（Servlet）：把"实时快照 + 历史数据"以 JSON 暴露给浏览器看板。
 *
 * <pre>
 * 端点一览：
 *   GET  /api/summary            → 实时快照 + 累计统计
 *   GET  /api/readings?limit=N   → 最近 N 条读数（折线图数据源）
 *   GET  /api/alarms             → 最近报警记录
 *   POST /api/command            → 下发控制，如 {"cmd":"set_temp_limit","value":21.5}
 * </pre>
 */
public class ApiServlet extends HttpServlet {

    private final Poller poller;
    private final MonitorDb db;
    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public ApiServlet(Poller poller, MonitorDb db) {
        this.poller = poller;
        this.db = db;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo() == null ? "" : req.getPathInfo();
        try {
            Object payload = switch (path) {
                case "/summary" -> summary();
                case "/readings" -> readings(intParam(req, "limit", 120));
                case "/alarms" -> db.recentAlarms(20);
                default -> null;
            };
            if (payload == null) {
                writeJson(resp, 404, Map.of("error", "未知端点: " + path));
            } else {
                writeJson(resp, 200, payload);
            }
        } catch (Exception e) {
            writeJson(resp, 500, Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo() == null ? "" : req.getPathInfo();
        try {
            if (!"/command".equals(path)) {
                writeJson(resp, 404, Map.of("error", "未知端点: " + path));
                return;
            }
            JsonObject body = JsonParser.parseReader(req.getReader()).getAsJsonObject();
            String cmd = body.has("cmd") ? body.get("cmd").getAsString() : "";
            if ("set_temp_limit".equals(cmd)) {
                double value = body.get("value").getAsDouble();
                if (value < 5 || value > 50) {
                    writeJson(resp, 400, Map.of("ok", false, "error", "上限范围 5.0 ~ 50.0℃"));
                    return;
                }
                poller.setTempLimit(value);
                writeJson(resp, 200, Map.of("ok", true,
                        "message", "已下发报警上限 %.1f℃（写寄存器 9）".formatted(value)));
            } else {
                writeJson(resp, 400, Map.of("ok", false, "error", "未知命令: " + cmd));
            }
        } catch (Exception e) {
            writeJson(resp, 500, Map.of("ok", false, "error", String.valueOf(e.getMessage())));
        }
    }

    /** 当前快照 + 累计统计。 */
    private Map<String, Object> summary() throws Exception {
        Poller.Snapshot s = poller.snapshot();
        var m = new LinkedHashMap<String, Object>();
        m.put("online", s.online());
        m.put("lastError", s.lastError());
        m.put("time", s.lastTime());
        m.put("temp", s.temp());
        m.put("humidity", s.humidity());
        m.put("light", s.light());
        m.put("soil", s.soil());
        m.put("limit", s.limit());
        m.put("alarm", s.alarm());
        m.put("deviceAlarms", s.deviceAlarms());
        m.put("polls", s.pollCount());
        m.put("totalReadings", db.countReadings());
        m.put("totalAlarms", db.countAlarms());
        return m;
    }

    private List<?> readings(int limit) throws Exception {
        return db.recentReadings(Math.min(Math.max(limit, 1), 600)).stream()
                .map(r -> {
                    var m = new LinkedHashMap<String, Object>();
                    m.put("time", r.timeStr());
                    m.put("temp", r.temp());
                    m.put("humidity", r.humidity());
                    m.put("light", r.light());
                    m.put("soil", r.soil());
                    m.put("alarm", r.alarm());
                    return m;
                })
                .toList();
    }

    private static int intParam(HttpServletRequest req, String name, int def) {
        try {
            return Integer.parseInt(req.getParameter(name));
        } catch (Exception e) {
            return def;
        }
    }

    private void writeJson(HttpServletResponse resp, int status, Object payload) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(gson.toJson(payload));
    }
}