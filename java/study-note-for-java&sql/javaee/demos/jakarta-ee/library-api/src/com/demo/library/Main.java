package com.demo.library;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

/**
 * 图书借阅管理 REST API —— 嵌入式 Tomcat + Servlet + JDBC + SQLite
 *
 * 这是一个真实可运行的 Jakarta EE Web 应用，请求处理链路：
 *   HTTP 请求 → Servlet（Web 层）→ DAO（数据层）→ SQLite（存储层）
 *
 * 启动后访问 http://localhost:8081/api/books
 */
public class Main {

    public static void main(String[] args) throws Exception {
        // 1. 初始化数据库（建表）
        Db.init();

        // 2. 启动嵌入式 Tomcat（Jakarta EE 10 / Servlet 6.0 容器）
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8081);
        tomcat.getConnector(); // 创建 HTTP 连接器（监听端口）

        // 3. 注册 Servlet 与 URL 映射
        Context ctx = tomcat.addContext("", null);
        Tomcat.addServlet(ctx, "bookApi", new BookApiServlet());
        ctx.addServletMappingDecoded("/api/books/*", "bookApi");

        // 4. 启动！
        tomcat.start();
        System.out.println("==========================================================");
        System.out.println("  图书借阅管理 API 已启动 (嵌入式 Tomcat 10.1 / Servlet 6.0)");
        System.out.println("  地址: http://localhost:8081/api/books");
        System.out.println("  (Ctrl+C 停止)");
        System.out.println("==========================================================");
        tomcat.getServer().await(); // 阻塞等待请求
    }
}
