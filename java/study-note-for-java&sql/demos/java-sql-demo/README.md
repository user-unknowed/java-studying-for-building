# Java × SQL 数据库 实战演示（java-sql-demo）

银行账户场景的 4 个递进式 Java 程序，真实演示 Java 如何与 SQL 数据库配合工作。
全部代码在本工作区终端 **实际编译并运行通过**（OpenJDK 17 + SQLite JDBC 3.47）。

## 运行环境

- JDK 17+（本工作区 Ubuntu 终端已内置）
- `lib/sqlite-jdbc.jar`（SQLite 的 JDBC 驱动；首次运行前执行 `bash download-driver.sh` 下载）

## 编译 & 运行

```bash
cd java-sql-demo

# 首次：下载 sqlite-jdbc 驱动（约 13MB，脚本会自动创建 lib/ 目录）
bash download-driver.sh

# 编译（一次性）

javac -encoding UTF-8 -d out -cp lib/sqlite-jdbc.jar src/*.java

# 依次运行 4 个示例（共用同一个数据库文件 data/bank.db）
java -Dfile.encoding=UTF-8 -cp 'lib/sqlite-jdbc.jar:out' Step1_JdbcBasics   data/bank.db
java -Dfile.encoding=UTF-8 -cp 'lib/sqlite-jdbc.jar:out' Step2_SqlInjection data/bank.db
java -Dfile.encoding=UTF-8 -cp 'lib/sqlite-jdbc.jar:out' Step3_Transaction  data/bank.db
java -Dfile.encoding=UTF-8 -cp 'lib/sqlite-jdbc.jar:out' Step4_BatchPerf    data/bank.db
```

## 各文件学什么

| 文件 | 主题 | 关键收获 |
|---|---|---|
| `Step1_JdbcBasics.java` | JDBC 六步 + CRUD + 对象映射 | "表的一行 = Java 的一个对象"；数据在库里，不在 Java 内存里 |
| `Step2_SqlInjection.java` | SQL 注入攻击与防御 | 字符串拼接会被 `' OR '1'='1` 击穿；PreparedStatement 预编译免疫 |
| `Step3_Transaction.java` | 转账事务 | 事务保证"要么全成要么全败"；没有它，100 元会凭空消失 |
| `Step4_BatchPerf.java` | 批量操作性能 | 实测 1000 条插入：逐条 5324ms → 批处理+事务 **9ms** |

## 数据文件

`data/bank.db` — 由 Java 程序真实写入的 SQLite 数据库文件。
它**独立于 Java 进程存在**，可用 Python 直接读取验证：

```bash
python3 -c "import sqlite3; c = sqlite3.connect('data/bank.db'); \
            print(list(c.execute('SELECT * FROM account')))"
```

## 运行输出存档

见 `run_output.txt`（本次运行的完整真实输出）。
