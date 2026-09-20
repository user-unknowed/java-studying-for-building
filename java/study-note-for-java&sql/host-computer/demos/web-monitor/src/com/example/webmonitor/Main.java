package com.example.webmonitor;

import com.example.webmonitor.db.MonitorDb;
import com.example.webmonitor.service.Poller;
import com.example.webmonitor.web.ApiServlet;
import com.example.webmonitor.web.DashboardServlet;
import com.example.webmonitor.web.LogFilter;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Web 版上位机入口：装配三个部件并启动嵌入式 Tomcat。
 *
 * <pre>
 * 部件组装（对应 07 号教程第 2 章）：
 *   Poller（采集线程）──存入──▶ MonitorDb（SQLite 历史库）
 *      │
 *      └─实时快照─▶ Servlet（API 层 + 看板页）──▶ 浏览器
 * </pre>
 *
 * 用法：
 * <pre>
 *   java -cp "lib/*:out" com.example.webmonitor.Main \
 *        [--port 8090] [--sim-host 127.0.0.1] [--sim-port 15020] [--interval 2]
 * </pre>
 */
public class Main {

    public static void main(String[] args) throws Exception {
        var opts = parse(args);
        int port = Integer.parseInt(opts.getOrDefault("port", "8090"));
        String simHost = opts.getOrDefault("sim-host", "127.0.0.1");
        int simPort = Integer.parseInt(opts.getOrDefault("sim-port", "15020"));
        int intervalSec = Integer.parseInt(opts.getOrDefault("interval", "2"));
        String dbFile = opts.getOrDefault("db", "data/web-monitor.db");
        String webDir = opts.getOrDefault("web", "web");

        // ① 历史数据库（SQLite）
        var db = new MonitorDb(dbFile);

        // ② 采集线程（后台，循环轮询下位机）
        var poller = new Poller(simHost, simPort, intervalSec * 1000L, db);
        poller.start();

        // ③ Web 层：嵌入式 Tomcat（Jakarta EE 10 / Servlet 6.0）
        var tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector(); // 创建 HTTP 连接器（监听端口）
        Context ctx = tomcat.addContext("", null);

        // API Servlet：/api/*
        Tomcat.addServlet(ctx, "api", new ApiServlet(poller, db));
        ctx.addServletMappingDecoded("/api/*", "api");

        // 看板页 Servlet：/ 与 /index.html
        Tomcat.addServlet(ctx, "dashboard", new DashboardServlet(webDir));
        ctx.addServletMappingDecoded("/", "dashboard");
        ctx.addServletMappingDecoded("/index.html", "dashboard");

        // 请求日志过滤器（Filter 教学示例：对所有路径生效）
        var filterDef = new FilterDef();
        filterDef.setFilterName("log");
        filterDef.setFilter(new LogFilter());
        ctx.addFilterDef(filterDef);
        var filterMap = new FilterMap();
        filterMap.setFilterName("log");
        filterMap.addURLPattern("/*");
        ctx.addFilterMap(filterMap);

        // 收尾钩子：Ctrl+C 时停采集、关数据库
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            poller.stop();
            try {
                db.close();
            } catch (Exception ignored) {
            }
        }));

        tomcat.start();
        System.out.println("==========================================================");
        System.out.println("  温室监控 · Web 版上位机已启动");
        System.out.println("  看板:  http://localhost:" + port + "/");
        System.out.println("  API:   http://localhost:" + port + "/api/summary");
        System.out.println("  下位机: Modbus TCP " + simHost + ":" + simPort + "（每 " + intervalSec + "s 轮询）");
        System.out.println("  (Ctrl+C 停止)");
        System.out.println("==========================================================");
        tomcat.getServer().await(); // 阻塞，直到进程退出
    }

    /** 解析 --key value 形式的参数（教学简版）。 */
    private static Map<String, String> parse(String[] args) {
        var map = new LinkedHashMap<String, String>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                String key = args[i].substring(2);
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    map.put(key, args[++i]);
                } else {
                    map.put(key, "true");
                }
            }
        }
        return map;
    }
}