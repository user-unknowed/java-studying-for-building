package com.demo.order;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

/**
 * 迷你订单服务 —— 订单创建/查询/取消 + 防超卖并发控制
 *
 * 核心链路：HTTP 请求 → Servlet → DAO（事务）→ SQLite
 *
 * 演示重点：
 *   ① 下单 = 扣库存 + 建订单 + 写明细（一个原子事务）
 *   ② 条件更新（stock >= ?）防超卖，并发下不卖超
 *   ③ 取消订单 = 状态变更 + 库存回滚（同一事务，防重复取消）
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Db.init();

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8082);
        tomcat.getConnector();

        Context ctx = tomcat.addContext("", null);

        Tomcat.addServlet(ctx, "productApi", new ProductApiServlet());
        ctx.addServletMappingDecoded("/api/products/*", "productApi");

        Tomcat.addServlet(ctx, "orderApi", new OrderApiServlet());
        ctx.addServletMappingDecoded("/api/orders/*", "orderApi");

        Tomcat.addServlet(ctx, "statsApi", new StatsApiServlet());
        ctx.addServletMappingDecoded("/api/stats/*", "statsApi");

        tomcat.start();
        System.out.println("==============================================================");
        System.out.println("  迷你订单服务已启动 (嵌入式 Tomcat 10.1 / Servlet 6.0)");
        System.out.println("  商品列表: GET  http://localhost:8082/api/products");
        System.out.println("  下单:     POST http://localhost:8082/api/orders");
        System.out.println("  统计:     GET  http://localhost:8082/api/stats/sales");
        System.out.println("==============================================================");
        tomcat.getServer().await();
    }
}