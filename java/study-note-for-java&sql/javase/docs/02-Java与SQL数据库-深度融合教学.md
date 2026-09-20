# Java 与 SQL 数据库 · 深度融合教学

> 目标：深刻理解 Java 和 SQL 数据库**各自的意义**、以及它们**如何配合**变成一个系统。
> 全部代码已在工作区真实编译运行（`java-sql-demo/`），本文引用的是**真实运行结果**。
> 整理：Operit AI ｜ 2026-09

---

## 0. 先回答一个灵魂问题：为什么需要"两个东西"？

**假设只有 Java，没有数据库：**
- 数据存在内存里 → 进程一停，数据全丢（没有持久化）；
- 多用户同时改同一条数据 → 互相覆盖（没有并发控制）；
- 想查"余额大于 1000 的客户按地区分组统计" → 手写几百行循环代码（没有声明式集合运算）。

**假设只有数据库，没有 Java：**
- 业务规则无法优雅表达（积分怎么算？风控怎么判？）；
- 无法对接界面、网络协议、第三方系统；
- 没有面向对象、没有生态。

**结论：**
> Java 管"怎么做业务"，数据库管"怎么守住数据"。
> 两者分工协作，**契约是 SQL + 事务**。

---

## 1. 分工模型：大脑与仓库

| 维度 | Java（应用层） | SQL 数据库（数据层） |
|---|---|---|
| 管什么 | 业务流程、规则判断、交互 | 数据的存储、索引、并发、事务 |
| 擅长 | 面向对象、复杂逻辑、生态 | 集合运算、持久化、一致性保证 |
| 生命周期 | 进程级（可重启、可多实例） | 数据级（比应用活得长） |
| 故障后果 | 服务暂时不可用，数据没事 | 数据损坏/丢失，灾难性 |
| 一句话 | "怎么做事" | "记住事实，并且不乱" |

---

## 2. 桥：JDBC（Java Database Connectivity）

- 1997 年诞生，至今仍是 **Java 访问一切数据库的底座**；
- **面向接口编程的教科书案例**：Java 代码只依赖 `java.sql.*` 接口，数据库厂商提供"驱动"实现；
- 数据流向：

```
Java 程序 → JDBC API → 驱动(jar包) → 数据库协议 → 数据库内核
```

- JDBC URL 的格式（换数据库只改 URL 和驱动）：
  - `jdbc:mysql://host:3306/db`
  - `jdbc:postgresql://host:5432/db`
  - `jdbc:sqlite:data/bank.db`

**JDBC 六步**（Step1 完整演示）：

```java
Class.forName("org.sqlite.JDBC");                 // 1. 加载驱动
try (Connection conn = DriverManager.getConnection(url)) {   // 2. 建立连接
    PreparedStatement ps = conn.prepareStatement(            // 3. 创建语句对象
        "INSERT INTO account(owner, balance) VALUES (?, ?)");
    ps.setString(1, "张三");                                  // 4. 绑定参数并执行
    ps.executeUpdate();
    ResultSet rs = ps.executeQuery("SELECT * FROM account"); // 5. 处理结果集
    while (rs.next()) { ... }
}                                                            // 6. 自动关闭资源
```

---

## 3. 映射：表 ↔ 对象（ORM 的起源）

这是理解 Java 与数据库关系的**核心心智模型**：

```
数据库的表      <===>  Java 的类
表中的一行      <===>  类的一个对象
表中的一列      <===>  对象的字段
外键引用        <===>  对象引用
```

Step1 真实运行结果——`ResultSet` 的每一行被"翻译"成 Java 对象：

```
[4] SELECT 结果映射为 Java 对象:
      Account{id=1, owner='张三', balance=1000.00}
      Account{id=2, owner='李四', balance=500.00}
```

> **阻抗失配（Object-Relational Impedance Mismatch）**：
> 对象有继承、引用、多态；关系表只有扁平的二维结构。
> 这个"缝隙"就是 ORM 框架（MyBatis / Hibernate）存在的意义。

---

## 4. 安全：SQL 注入（Step2 真实攻击演示）

**原理**：字符串拼接 SQL，让用户的"输入数据"越界成了"代码"。

真实攻击结果（[危险] 字符串拼接写法）：

```
输入: ' OR '1'='1
SQL:  SELECT owner, balance FROM account WHERE owner = '' OR '1'='1'
     查到 → 张三, 余额 1000.0     ← 全表数据泄露！
     查到 → 李四, 余额 500.0
```

同样这个输入，在 [安全] PreparedStatement 写法下：

```
SQL: SELECT owner, balance FROM account WHERE owner = ?   (参数: ' OR '1'='1)
     共 0 条                     ← 攻击字符串只是普通字符，毫无破坏力
```

**为什么预编译能免疫？**
> 预编译让"SQL 结构"先定型上膛，参数永远是"子弹"——无论参数长什么样，都不会变成"枪的一部分"。

**工业延伸**：MyBatis 的 `#{}` 就是 PreparedStatement 占位符（安全）；`${}` 是字符串拼接（危险，仅限动态表名等白名单场景）。学到这里，你就能从原理上解释"为什么面试必问 #{} 和 ${} 的区别"。

---

## 5. 一致性：事务（Step3 真实转账演示）

**场景**：张三转 100 元给李四 = 两条 UPDATE，必须**同生共死**。

**真实运行结果回顾：**

| 场景 | 过程 | 结果 |
|---|---|---|
| ① 正常转账 | 扣款 + 收款 → commit | 张三 900 / 李四 600，总资产不变 ✓ |
| ② 收款方不存在 → rollback | 扣款后失败 → 全部回滚 | 张三 900 不变，钱没少 ✓ |
| ③ **没有事务**保护 | 扣款成功（自动提交）后系统故障 | 张三 800，**100 元凭空消失** ✗ |

**Java 事务标准模板**（工业代码的骨架）：

```java
conn.setAutoCommit(false);          // 开启事务
try {
    // ... 扣款 UPDATE ...
    // ... 收款 UPDATE ...
    conn.commit();                  // 全部成功才提交
} catch (SQLException e) {
    conn.rollback();                // 任何失败全部回滚
    throw e;
} finally {
    conn.setAutoCommit(true);       // 恢复默认模式
}
```

**为什么事务必须由数据库提供？**
因为只有数据库知道"这两条 UPDATE 是一个整体"——崩溃恢复、并发隔离、锁管理都发生在数据库层，Java 单方面保证不了。

**工业延伸**：Spring 的 `@Transactional` 注解，本质就是把上面这段模板代码用 AOP 自动包在了方法外面。

---

## 6. 效率：批处理与"落盘合并"（Step4 真实实测）

**1000 条插入，三种写法的真实耗时：**

| 写法 | 耗时 | 原因 |
|---|---|---|
| (a) 逐条执行 | 5324 ms | 每条 autocommit = 一次磁盘 fsync |
| (b) 批处理（无事务） | 5045 ms | 网络/接口开销省了，但落盘次数没省 |
| (c) **批处理 + 事务** | **9 ms** | 1000 次 fsync 合并成 1 次 |

> 约 **600 倍** 的差距。这是本次实操最震撼的数字——同样的 Java 代码能力，
> 懂不懂数据库的落盘机制，性能天壤之别。

**工业标配**：数据导入、日志批量写、ETL，全部使用"批处理 + 事务"。
（在网络数据库如 MySQL 上，批处理还能额外减少网络往返次数，差距会更明显。）

---

## 7. 持久化：数据不属于任何程序

Step4 之后，我们用 **Python** 读取了 **Java 写的**数据库文件：

```
== 用 Python 直接读取 Java 写下的数据库文件 ==
account 表:
   (1, '张三', 800.0)
   (2, '李四', 600.0)
tx_log 条数: 1000
```

**架构直觉**（本课最重要的认知升级）：

- 数据库不是"Java 程序的一部分"，而是**独立的一层**；
- Java 进程可以重启、可以多实例、甚至可以换成 Go/Python 重写——**数据安然无恙**；
- 所以架构上叫它"**数据层（Data Layer）**"：应用是"租客"，数据是"房子"。

---

## 8. 从 JDBC 到 MyBatis：工业上究竟怎么写

| 阶段 | 工具 | 解决了什么 | 新代价 |
|---|---|---|---|
| 手工时代 | JDBC | 能连数据库了 | 样板代码多、易写注入 |
| 轻封装 | Spring JdbcTemplate | 消灭样板代码 | SQL 仍散落各处 |
| **半自动 ORM** | **MyBatis** | SQL 与 Java 分离、映射自动化 | XML/注解配置 |
| 全自动 ORM | JPA / Hibernate | 基本不写 SQL | 复杂查询易失控 |
| 增强 | MyBatis-Plus | 单表 CRUD 零 SQL | 只适合简单场景 |

- **连接池（HikariCP）**：连接是稀缺资源，工业上绝不"用完就关"，而是池化复用；
- 学习路径建议：**原理从 JDBC 学起（你已经完成），工程用 MyBatis / MyBatis-Plus，读别人的代码也不怕 JPA**。

---

## 9. 完整心智模型（一句话总结全课）

```
用户请求
   ↓
Java 业务逻辑（事务边界、校验、流程）      ← Spring Boot / 你的代码
   ↓
JDBC / MyBatis（SQL + 参数）              ← 桥梁：契约是 SQL
   ↓
数据库（索引 / 事务 / 锁 / MVCC）          ← MySQL / PostgreSQL / SQLite
   ↓
结果集 → Java 对象 → 返回响应
```

> 口诀：**Java 管逻辑，库管数据；接口是 SQL，契约是事务。**
> 分层各司其职——少了哪一层，都撑不起一个真实系统。

---

## 10. 练习建议（改代码玩，从易到难）

1. **Step1 加功能**：实现 `findByName(String name)`，返回单个 `Account` 对象；
2. **Step3 加业务**：给转账加"余额不足"校验。思考：先 SELECT 查余额再判断，还是直接用
   `UPDATE ... WHERE balance >= ?` 让数据库来判？哪个更安全？为什么？
3. **Step4 改参数**：把 N 改成 10000 再看差距；再试试"只加批处理、去掉事务"验证结论；
4. **挑战**：用两个线程模拟"同时给同一个人转账"，观察数据库的并发行为；
5. **进阶**：如果有 MySQL 环境，把 URL 换成 `jdbc:mysql://...`，体会
   "换数据库只改一行代码"——这就是 JDBC 抽象层的意义。

---

## 附录：配套文件与运行方式

```
java-sql-demo/
├── src/Step1_JdbcBasics.java     # JDBC 六步 + CRUD + 对象映射
├── src/Step2_SqlInjection.java   # SQL 注入攻防
├── src/Step3_Transaction.java    # 转账事务
├── src/Step4_BatchPerf.java      # 批处理性能
├── lib/sqlite-jdbc.jar           # JDBC 驱动
├── data/bank.db                  # 真实数据文件（Java 写、Python 可读）
├── out/                          # 编译产物
├── README.md                     # 编译运行说明
└── run_output.txt                # 本次运行输出存档
```

运行方式（终端）：

```bash
cd java-sql-demo
javac -encoding UTF-8 -d out -cp lib/sqlite-jdbc.jar src/*.java
java -Dfile.encoding=UTF-8 -cp 'lib/sqlite-jdbc.jar:out' Step1_JdbcBasics data/bank.db
# ... Step2 / Step3 / Step4 同理
```

**下一步学习衔接**：MyBatis 实战 → 连接池调优 → Spring Boot + MySQL 整合项目（可随时开讲）。