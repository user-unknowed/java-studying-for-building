package com.learn.order;

import java.io.File;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 电商订单服务 —— 启动类
 *
 * <p>@SpringBootApplication = @SpringBootConfiguration + @EnableAutoConfiguration + @ComponentScan
 * 一行注解背后：自动配置（内嵌 Tomcat、DataSource、Jackson、事务管理器……）全由框架完成。</p>
 */
@SpringBootApplication
public class OrderServiceApplication {

    public static void main(String[] args) {
        // 确保 SQLite 数据库目录存在（首次运行时自动创建）
        new File("data").mkdirs();
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
