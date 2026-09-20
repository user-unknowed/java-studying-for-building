package com.example.webmonitor.web;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 看板页服务：把 web/dashboard.html 返回给浏览器。
 *
 * <p>演示"静态页面如何被 Web 层托管"的最小实现：
 * 读取文件 → 设置 Content-Type → 写出字节。真实项目会交给默认 Servlet / Nginx，
 * 但手写一遍能看清"网页是怎么被送出去的"。
 */
public class DashboardServlet extends HttpServlet {

    private final Path htmlFile;

    public DashboardServlet(String webDir) {
        this.htmlFile = Path.of(webDir, "dashboard.html");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!Files.isRegularFile(htmlFile)) {
            resp.setStatus(500);
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().write("dashboard.html 未找到: " + htmlFile.toAbsolutePath());
            return;
        }
        byte[] bytes = Files.readAllBytes(htmlFile);
        resp.setStatus(200);
        resp.setContentType("text/html; charset=UTF-8");
        resp.setContentLength(bytes.length);
        resp.getOutputStream().write(bytes);
    }
}