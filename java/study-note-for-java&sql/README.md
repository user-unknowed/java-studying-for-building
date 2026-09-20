# study-note-for-java&sql

> ☕ + 🗄️ **Java / SQL 系统化学习笔记与可运行演示合集**
>
> 从 **JavaSE 基础 → Java×SQL 融合 → SQL 工业实践 → JavaEE 服务端开发**，递进式四阶梯。
> 所有演示代码均经过真实编译、运行、验证（拒绝纸上谈兵）。
>
> 整理：Operit AI · 2026-09

## 📂 分区结构

```
study-note-for-java&sql/
├── javase/                        # JavaSE 与 SQL 地基
│   ├── docs/                      # 01 ~ 04 号文档
│   └── demos/                     # javase-demo · java-sql-demo · sql-demo
└── javaee/                        # Java EE（Jakarta EE）企业级开发
    ├── README.md                  # 分区导航（推荐从这里开始阅读）
    ├── docs/                      # 05 号文档：0~11 章实战教程
    └── demos/
        ├── jakarta-ee/            # 原生版：library-api · order-service
        └── spring-boot/           # 现代版：order-service · shortlink-service
```

## 📖 文档导航（按学习顺序）

| # | 文档 | 核心内容 |
|:---:|---|---|
| 01 | [JavaSE基础知识点-完整梳理](javase/docs/01-JavaSE基础知识点-完整梳理.md) | Java 地基：运行机制、类型陷阱、面向对象、集合、异常、IO、多线程、反射注解 |
| 02 | [Java与SQL数据库-深度融合教学](javase/docs/02-Java与SQL数据库-深度融合教学.md) | JDBC、SQL 注入攻防、事务转账、批处理性能 |
| 03 | [SQL数据库深度讲解-工业实践版](javase/docs/03-SQL数据库深度讲解-工业实践版.md) | 窗口函数、索引优化、事务并发、执行计划 |
| 04 | [学习报告-Java-数据库-AI-Agent](javase/docs/04-学习报告-Java-数据库-AI-Agent.md) | 三大学习方向总览与路线规划 |
| 05 | [JavaEE企业级开发-现代实战教程](javaee/docs/05-JavaEE企业级开发-现代实战教程.md) | Servlet → DAO/事务 → REST → 并发防超卖 → 幂等 → Spring Boot 迁移 → 工程化踩坑（0~11 章） |

## 💻 可运行项目（全部实测通过）

### javase 分区

| 项目 | 技术栈 | 内容 |
|---|---|---|
| [javase-demo](javase/demos/javase-demo/) | Java | 字节码反汇编、类型陷阱（溢出/精度/常量池）、OOP 多态、集合 + Stream |
| [java-sql-demo](javase/demos/java-sql-demo/) | Java + JDBC + SQLite | CRUD、SQL 注入攻防实录、转账事务、批处理性能对比 |
| [sql-demo](javase/demos/sql-demo/) | Python + SQLite | JOIN、窗口函数、连续登录、EXPLAIN、索引实战（零依赖） |

### javaee 分区（详细导航见 [javaee/README.md](javaee/README.md)）

| 项目 | 技术栈 | 端口 | 练什么 |
|---|---|---|---|
| [library-api](javaee/demos/jakarta-ee/library-api/) | Jakarta EE · 嵌入式 Tomcat | 8081 | Servlet 路由、DAO 分层、事务借还、JOIN 统计 |
| [order-service（原生）](javaee/demos/jakarta-ee/order-service/) | Jakarta EE · 嵌入式 Tomcat | 8082 | 防超卖条件更新、状态守卫、15 并发压测 |
| [order-service（Spring Boot）](javaee/demos/spring-boot/order-service/) | Spring Boot 3.5 | 8081 | 注解、IoC、`@Transactional`、与原生版逐行对照 |
| [shortlink-service](javaee/demos/spring-boot/shortlink-service/) | Spring Boot 3.5 | 8083 | 302 跳转、唯一约束、原子计数 |

## 🗺️ 建议学习路线

```
① JavaSE 基础      javase/docs/01  +  javase/demos/javase-demo
② Java × SQL 融合  javase/docs/02  +  javase/demos/java-sql-demo
③ SQL 工业实践     javase/docs/03  +  javase/demos/sql-demo
④ JavaEE 服务端    javaee/docs/05  +  javaee/demos/（先原生，后 Spring Boot）
```

> 每一步都遵循同一原则：**先读文档懂原理，再跑代码看真实结果**。
