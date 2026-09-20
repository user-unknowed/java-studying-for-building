package com.learn.shortlink;

import java.io.File;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 短链接服务 —— 启动类
 *
 * <p>业务场景：把长网址压缩成「域名/短码」形式；访问短链时 302 跳转到原址并计数。
 * 现代互联网的基础服务之一（微博、短信、二维码背后的常见基建）。</p>
 */
@SpringBootApplication
public class ShortlinkApplication {

    public static void main(String[] args) {
        // 确保 SQLite 数据库目录存在（首次运行时自动创建）
        new File("data").mkdirs();
        SpringApplication.run(ShortlinkApplication.class, args);
    }
}
