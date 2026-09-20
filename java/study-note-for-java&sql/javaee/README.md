# JavaEE 分区 —— Java EE（Jakarta EE）企业级开发

> JavaSE 分区（`javase/`）之后的下一个台阶：从 Servlet 原理到 Spring Boot 工程化，配四个真实可运行项目。

## 📖 文档

| 文档 | 内容 |
|---|---|
| [05-JavaEE企业级开发-现代实战教程](docs/05-JavaEE企业级开发-现代实战教程.md) | 0~13 章：**现代 Java 语法速览 + 外部函数库与依赖管理** → 技术全景 → Servlet → DAO/事务 → REST → 并发/防超卖 → 幂等 → Spring Boot 迁移 → 工程化踩坑 → 收官 |

## 💻 项目（全部真实编译运行验证）

| 项目 | 技术栈 | 端口 | 练什么 |
|---|---|---|---|
| [library-api](demos/jakarta-ee/library-api/) | Jakarta EE · 嵌入式 Tomcat | 8081 | Servlet 路由、DAO 分层、事务、JOIN 统计 |
| [order-service](demos/jakarta-ee/order-service/) | Jakarta EE · 嵌入式 Tomcat | 8082 | 防超卖条件更新、状态守卫、15 并发压测 |
| [order-service（Spring Boot 版）](demos/spring-boot/order-service/) | Spring Boot 3.5 | 8081 | 注解、IoC、@Transactional、与原生版对照 |
| [shortlink-service](demos/spring-boot/shortlink-service/) | Spring Boot 3.5 | 8083 | 302 跳转、唯一约束、原子计数 |

## 🚀 快速开始

**Spring Boot 项目**（以 order-service 为例）：

```bash
cd demos/spring-boot/order-service
mvn -q -DskipTests package
java -jar target/order-service-1.0.0.jar
# 另开终端：
bash test_api.sh | tee run_output.txt     # 接口测试
python3 stress_test.py 15 3 1             # 并发压测
```

**原生 Jakarta EE 项目**（以 order-service 为例，纯 javac，无 Maven）：

```bash
cd demos/jakarta-ee/order-service
bash download-deps.sh                     # 首次：下载依赖 jar 到 lib/
javac -encoding UTF-8 -cp "lib/*" -d out src/com/demo/order/*.java
java -Dfile.encoding=UTF-8 -cp "out:lib/*" com.demo.order.Main
# 另开终端：
bash test_api.sh | tee run_output.txt
python3 stress_test.py 15 3 1
```

## 🗺️ 学习路径建议

1. 先读第 0~4 章（原理 + 新增的现代语法速览与依赖管理），对技术全景建立认知；
2. 再读第 5~7 章（Servlet → DAO 分层 → REST 设计），边读边跑 `library-api`；
3. 接着读第 8~10 章（并发 / 事务 / 幂等），对照 `order-service` 两版代码；
4. 最后读第 11~13 章（迁移 / 工程化 / 收官），跑 `shortlink-service`。

## 🔗 延伸阅读

- [07-JavaEE上位机实战：网页监控看板与远程控制](../host-computer/docs/07-JavaEE上位机实战-网页监控看板与远程控制.md) ——用本分区的 Servlet / 嵌入式 Tomcat 技能，给"工业设备"做一块 Web监控看板；配套项目 [web-monitor](../host-computer/demos/web-monitor/)与 05 号教程的 `library-api`同构。

---
*本分区与 `javase/`、`host-computer/` 分区共同构成完整的 Java 学习阶梯：JavaSE → Java×SQL → SQL 工业实践 → JavaEE 服务端开发 → 工业应用。*