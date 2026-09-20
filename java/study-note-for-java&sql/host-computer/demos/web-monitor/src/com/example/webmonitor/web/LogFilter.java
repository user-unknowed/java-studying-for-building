package com.example.webmonitor.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

/**
 * 请求日志过滤器 —— Java EE "Filter" 的最小可运行示例。
 *
 * <p>给每个 HTTP 请求打印一行日志（方法 / 路径 / 耗时），
 * 在 Main 中注册到 "/*"（对所有路径生效）。
 *
 * <p>Filter 与 Servlet 的关系：请求先过 Filter 链 → 再到 Servlet；响应原路返回。
 * 统一鉴权、日志、跨域（CORS）头之类的"横切逻辑"都放在这里。
 */
public class LogFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        var http = (HttpServletRequest) request;
        long t0 = System.currentTimeMillis();
        try {
            chain.doFilter(request, response); // 放行：交给下一个过滤器 / Servlet
        } finally {
            System.out.printf("[web] %s %s (%d ms)%n",
                    http.getMethod(), http.getRequestURI(), System.currentTimeMillis() - t0);
        }
    }
}