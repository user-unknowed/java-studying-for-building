# study-note-for-java&sql

> ☕ + 🗄️ **Java / SQL 系统化学习笔记与可运行演示合集**
>
> 从 **JavaSE 基础 → Java×SQL 融合 → SQL 工业实践**，递进式三阶梯。
> 所有演示代码均经过真实编译、运行、验证（拒绝纸上谈兵）。
>
> 整理：Operit AI · 2026-09

## 📖 文档导航（按学习顺序）

| # | 文档 | 核心内容 |
|:---:|---|---|
| 01 | [JavaSE基础知识点-完整梳理](docs/01-JavaSE基础知识点-完整梳理.md) | Java 地基：运行机制、类型陷阱、面向对象、集合、异常、IO、多线程、反射注解 |
| 02 | [Java与SQL数据库-深度融合教学](docs/02-Java与SQL数据库-深度融合教学.md) | JDBC、SQL 注入攻防、事务转账、批处理性能 —— Java 视角看数据库 |
| 03 | [SQL数据库深度讲解-工业实践版](docs/03-SQL数据库深度讲解-工业实践版.md) | 窗口函数、索引优化、事务并发、执行计划 —— 数据库视角的工业实践 |
| 04 | [学习报告-Java-数据库-AI-Agent](docs/04-学习报告-Java-数据库-AI-Agent.md) | 三大学习方向总览与路线规划 |

## 💻 可运行演示（全部实测通过）

| 项目 | 技术栈 | 内容 |
|---|---|---|
| [javase-demo](demos/javase-demo/) | Java | 字节码反汇编、类型陷阱（溢出/精度/常量池）、OOP 多态、集合 + Stream |
| [java-sql-demo](demos/java-sql-demo/) | Java + JDBC + SQLite | CRUD、SQL 注入攻防实录、转账事务、批处理性能对比（5324ms → 9ms） |
| [sql-demo](demos/sql-demo/) | Python + SQLite | JOIN、窗口函数、连续登录、EXPLAIN、索引实战（零依赖） |

## 🗺️ 建议学习路线

```
① JavaSE 基础    docs/01  +  demos/javase-demo     ← Java 语言地基
② Java × SQL     docs/02  +  demos/java-sql-demo   ← 用 Java 操作数据库
③ SQL 工业实践   docs/03  +  demos/sql-demo        ← 深入数据库本身
```

## 🚀 快速开始

### javase-demo（Java 17+）

```bash
cd demos/javase-demo
javac -encoding UTF-8 -d out src/*.java
java -Dfile.encoding=UTF-8 -cp out Demo1_Basics   # 也可运行 Demo2_Oop / Demo3_Collections
```

### java-sql-demo（Java 17+）

```bash
cd demos/java-sql-demo
bash download-driver.sh           # 首次：下载 sqlite-jdbc 驱动
javac -encoding UTF-8 -d out -cp lib/sqlite-jdbc.jar src/*.java
java -Dfile.encoding=UTF-8 -cp 'lib/sqlite-jdbc.jar:out' Step1_JdbcBasics data/bank.db
# 数据库文件 data/bank.db 会在首次运行时自动创建并初始化
```

### sql-demo（Python 3）

```bash
cd demos/sql-demo
python3 sql_demo.py
```

## 📁 目录结构

```
study-note-for-java&sql/
├── README.md            ← 本导航
├── docs/                ← 教学文档（按学习顺序编号）
└── demos/               ← 可运行演示项目
    ├── javase-demo/     JavaSE 基础演示（4 个程序）
    ├── java-sql-demo/   Java × JDBC 实战（4 个程序）
    └── sql-demo/        SQL 工业实践（Python + SQLite）
```

---

### 说明

- 编译产物（`out/`、`*.class`）、本地数据库文件（`data/bank.db`）、JDBC 驱动（约 13MB）未入库，均可按上方命令一键重新生成 / 下载。
- 各演示的详细说明见各自目录下的 README。
