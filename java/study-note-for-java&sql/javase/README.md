# JavaSE 分区 —— Java 语言与 SQL 地基

> 整条学习阶梯的第一段：从 Java 语言地基，到 Java×SQL 融合，再到 SQL 工业实践。

## 📖 文档

| # | 文档 | 核心内容 |
|:---:|---|---|
| 01 | [JavaSE基础知识点-完整梳理](docs/01-JavaSE基础知识点-完整梳理.md) | Java 地基（0~17 章）：运行机制、类型陷阱、面向对象、集合、异常、IO、多线程、反射注解、**现代语法全家桶、外部函数库、网络编程** |
| 02 | [Java与SQL数据库-深度融合教学](docs/02-Java与SQL数据库-深度融合教学.md) | JDBC、SQL 注入攻防、事务转账、批处理性能 —— Java 视角看数据库 |
| 03 | [SQL数据库深度讲解-工业实践版](docs/03-SQL数据库深度讲解-工业实践版.md) | 窗口函数、索引优化、事务并发、执行计划 —— 数据库视角的工业实践 |
| 04 | [学习报告-Java-数据库-AI-Agent](docs/04-学习报告-Java-数据库-AI-Agent.md) | 三大学习方向总览与路线规划 |

## 💻 项目（全部真实编译运行验证）

| 项目 | 技术栈 | 练什么 |
|---|---|---|
| [javase-demo](demos/javase-demo/) | Java | 字节码反汇编、类型陷阱、OOP 多态、集合 + Stream、现代语法 / 外部库 / 网络（Demo1~6）+ JUnit |
| [java-sql-demo](demos/java-sql-demo/) | Java + JDBC + SQLite | CRUD、SQL 注入攻防、转账事务、批处理性能 |
| [sql-demo](demos/sql-demo/) | Python + SQLite | JOIN、窗口函数、连续登录、EXPLAIN、索引实战（零依赖） |

## 🚀 快速开始

**javase-demo**（先跑起来，再看字节码）：

```bash
cd demos/javase-demo
bash download-deps.sh                                 # 首次：下载 Demo5/6 所需外部库
javac -encoding UTF-8 -cp "lib/*" -d out src/*.java
java -Dfile.encoding=UTF-8 -cp out Demo1_Basics
java -Dfile.encoding=UTF-8 -cp "out:lib/*" Demo5_Libs  # 外部库演示
javap -c -cp out Hello                                # 看 1+2 如何变成 JVM 指令
```

**java-sql-demo**（Java 如何连数据库）：

```bash
cd demos/java-sql-demo
bash download-driver.sh          # 首次：下载 sqlite-jdbc 驱动到 lib/
javac -encoding UTF-8 -d out -cp lib/sqlite-jdbc.jar src/*.java
java -Dfile.encoding=UTF-8 -cp 'lib/sqlite-jdbc.jar:out' Step3_Transaction data/bank.db
```

**sql-demo**（零依赖，直接跑）：

```bash
cd demos/sql-demo
python3 sql_demo.py
```

## 🗺️ 学完之后

1. 进入下一台阶 👉 [javaee 分区](../javaee/README.md)：Servlet 原理 → 事务 / 并发 / 幂等 → Spring Boot 工程化。
2. 工业应用方向 👉 [host-computer 分区](../host-computer/README.md)：串口 / Modbus / 数据采集与监控（前置：本篇 01 的第 14~16 章）。

---
*本分区与 `javaee/`、`host-computer/` 分区共同构成完整的 Java 学习阶梯：JavaSE → Java×SQL → SQL 工业实践 → JavaEE 服务端开发 → 工业应用。*