# SQL数据库深度讲解（工业实践版）

> 面向：想系统掌握 SQL 与数据库的开发者 ｜ 整理：Operit AI ｜ 2026-09
> 配套资料：`sql-demo/sql_demo.py`（可真实运行）、`sql-demo/demo_output.txt`（真实运行输出）
> 姊妹篇：《学习报告-Java-数据库-AI-Agent.md》

---

## 0. 导读：工业界需要什么样的数据库能力

三条主线：

| 能力 | 具体表现 | 对应章节 |
|---|---|---|
| **写得出** | 复杂查询、窗口函数、多表分析 | 第 1～3 章 |
| **跑得快** | 索引设计、执行计划、慢查询治理 | 第 4 章 |
| **不出事** | 事务、锁、一致性、安全 | 第 5～6 章 |

各岗位的日常视角：
- **后端开发**：表设计 + CRUD + 索引优化 + 事务控制 + Java 代码接入（第 7 章）
- **数据开发 / 分析师**：复杂 SQL、窗口函数、ETL、报表（第 3 章）
- **DBA / SRE**：复制、备份、分库分表、故障处理（第 6 章）

---

## 1. 关系模型与表设计

### 1.1 核心概念一句话版

表=二维结构；行=记录；列=字段；**主键**=唯一标识（InnoDB 中主键就是聚簇索引）；**外键**=引用完整性；约束=NOT NULL / UNIQUE / CHECK / DEFAULT。

### 1.2 设计原则

- 三大范式：1NF 列不可再分；2NF 消除部分依赖；3NF 消除传递依赖。
- 工业实践：**不迷信范式，适度反范式**。例：订单表冗余"下单时的商品名 / 收货地址"——商品可能改名，订单要保留**历史快照**。
- 校验分工：数据库约束兜底 + 应用层校验为主（错误信息更友好）。

### 1.3 字段类型选择速查表

| 场景 | 推荐类型 | 说明 |
|---|---|---|
| 主键 | BIGINT UNSIGNED 自增 / 雪花 ID | 分布式系统用趋势递增的雪花 ID，避免写入热点 |
| 金额 | DECIMAL(10,2) | **绝不用 FLOAT/DOUBLE**（二进制浮点误差：0.1+0.2≠0.3） |
| 时间 | DATETIME / TIMESTAMP | TIMESTAMP 有 2038 问题；高精度用 DATETIME(3) |
| 短字符串 | VARCHAR(N) | N 影响排序内存与索引大小，按业务估算 |
| 布尔 | TINYINT(1) | MySQL 没有真正的 BOOLEAN |
| 半结构化 | JSON / JSONB(PG) | PG 的 jsonb 可建索引，更推荐 |
| 枚举 | TINYINT + 字典表 | 比 ENUM 容易演进（加值不改表结构） |

### 1.4 字符集

一律 **utf8mb4**——MySQL 的 "utf8" 是 3 字节残缺版，存不了 emoji 等 4 字节字符。排序规则常用 utf8mb4_unicode_ci / utf8mb4_0900_ai_ci。

### 1.5 建表"工业三件套"

```sql
id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
```

---

## 2. SQL 语言全景与执行顺序

### 2.1 语言分类

| 类别 | 语句 | 用途 |
|---|---|---|
| DDL | CREATE / ALTER / DROP / TRUNCATE | 定义结构 |
| DML | INSERT / UPDATE / DELETE | 修改数据 |
| DQL | SELECT | 查询 |
| DCL | GRANT / REVOKE | 权限 |
| TCL | BEGIN / COMMIT / ROLLBACK / SAVEPOINT | 事务 |

### 2.2 逻辑执行顺序（面试必问、优化基础）

```
FROM → JOIN/ON → WHERE → GROUP BY → HAVING → SELECT → DISTINCT → ORDER BY → LIMIT
```

推论：
- WHERE 里不能用 SELECT 定义的别名（SELECT 还没执行）→ 用子查询或重复表达式；
- 聚合过滤必须用 HAVING；
- 能尽早过滤就尽早过滤：能在 WHERE 做的别拖到 HAVING / ORDER BY。

### 2.3 NULL 与三值逻辑（高频陷阱）

- `NULL = NULL` 结果是 NULL（不是 TRUE）→ 判断用 `IS NULL` / `IS NOT NULL`；
- `COUNT(*)` 数行数；`COUNT(列)` 忽略 NULL；
- **`NOT IN (子查询含 NULL)` 会整体失效、返回空集** → 改用 `NOT EXISTS`；
- 空值函数：`COALESCE`（标准）、`IFNULL`（MySQL）、`NULLIF(a,b)`（相等则返回 NULL，可用于防除零）。

---

## 3. 查询编程核心技能

### 3.1 JOIN 家族

| 类型 | 含义 | 典型场景 |
|---|---|---|
| INNER JOIN | 交集 | 常规关联 |
| LEFT JOIN | 左表全保留 | 主表 + 可选关联（"没下过单也要列出用户"） |
| RIGHT JOIN | 右表全保留 | 少用，习惯改写为 LEFT |
| FULL JOIN | 并集 | PG/Oracle 支持；MySQL 用 UNION 模拟 |
| CROSS JOIN | 笛卡尔积 | 生成序列、组合枚举 |
| 自连接 | 表与自己连 | 层级（员工-经理）、行间对比 |

真实运行示例（结果见附录 A）：

```sql
SELECT e.name AS 员工, d.name AS 部门, COALESCE(m.name, '—') AS 经理
FROM emp e
JOIN dept d ON e.dept_id = d.id      -- 内连接
LEFT JOIN emp m ON e.mgr_id = m.id;  -- 左连接 + 自连接
```

### 3.2 子查询 vs EXISTS vs JOIN

- 标量子查询：返回单值，可放 SELECT / WHERE；
- IN：适合"子查询结果集小"；
- EXISTS：短路语义，适合"子查询大、命中即止"（找到一条就不再扫）；
- 很多场景 JOIN 可替代且更可控；核心直觉是**"小表驱动大表"**；
- 注意去重语义：JOIN 可能放大行数（一对多），EXISTS 不会。

### 3.3 聚合与分组

- GROUP BY + HAVING 是报表骨架；
- MySQL：`WITH ROLLUP` 出小计/总计；`GROUP_CONCAT` 聚合字符串；
- **ONLY_FULL_GROUP_BY**（MySQL 5.7+ 默认开启）：非聚合列必须出现在 GROUP BY 里 → 保证结果确定性。

### 3.4 窗口函数（工业分水岭）

语法：`函数() OVER (PARTITION BY 分组 ORDER BY 排序 [ROWS/RANGE 帧])`

三大类：

| 类别 | 函数 | 典型场景 |
|---|---|---|
| 排名 | ROW_NUMBER / RANK / DENSE_RANK | 排名、去重、TopN |
| 偏移 | LAG / LEAD / FIRST_VALUE / LAST_VALUE | 环比同比、前后行对比 |
| 聚合 | SUM / COUNT / AVG / MAX / MIN ... OVER | 累计值、移动平均、占比 |

三个必背模板：

```sql
-- ① 分组 TopN（窗口函数在 WHERE 之后计算，不能直接 WHERE rn<=N，要套一层）
SELECT * FROM (
  SELECT *, ROW_NUMBER() OVER (PARTITION BY 组 ORDER BY 值 DESC) AS rn
  FROM t
) x WHERE rn <= 3;

-- ② 累计值（默认帧：分组内首行 → 当前行）
SELECT dt, amt, SUM(amt) OVER (ORDER BY dt) AS running_total FROM t;

-- ③ 环比（与上一行对比）
SELECT dt, amt,
       LAG(amt) OVER (ORDER BY dt) AS prev_amt,
       amt - LAG(amt) OVER (ORDER BY dt) AS diff
FROM t;
```

真实运行结果节选（注意市场部并列薪资下的行号/名次差异）：

| 员工 | 部门 | 薪资 | ROW_NUMBER | RANK |
|---|---|---|---|---|
| 赵敏 | 2 | 20000 | 1 | 1 |
| 刘洋 | 2 | 20000 | 2 | **1** ← 并列同名次 |

### 3.5 CTE 与递归 CTE

- `WITH cte AS (...)` 把复杂 SQL 分层，可读性大幅提升；
- 递归模板（组织树 / 菜单树 / 路径搜索）：

```sql
WITH RECURSIVE tree AS (
  SELECT id, name, 1 AS lvl FROM org WHERE parent_id IS NULL   -- 起点：根节点
  UNION ALL
  SELECT o.id, o.name, t.lvl + 1
  FROM org o JOIN tree t ON o.parent_id = t.id                 -- 递归：挂到上级下面
)
SELECT * FROM tree;
```

### 3.6 经典工业题型库

| 题型 | 方法 | 关键点 |
|---|---|---|
| 连续 N 天登录 | 日期 − 行号 = 常数分组 | 窗口函数 + GROUP BY（附录 A 有完整可运行版） |
| 分组 TopN | ROW_NUMBER 套子查询 | 窗口函数计算时机 |
| 行转列 | CASE WHEN + SUM | MySQL 无 PIVOT 语法 |
| 同比 / 环比 | LAG / 自连接偏移 | 注意缺月补零 |
| 第 N 高薪水 | DENSE_RANK / LIMIT OFFSET | "并列名次"的语义 |
| 留存率 / 复购率 | 首次活跃日 + 日期差 + COUNT DISTINCT | 先想清楚"数据粒度" |
| 中位数 | 窗口函数 + 位置判定 | 区分奇偶个数 |

---

## 4. 性能优化：索引与执行计划

### 4.1 为什么是 B+ 树

- 非叶子节点只存键 → 一个节点塞更多键 → 树更矮（3～4 层可撑千万级数据）；
- 叶子节点存数据且用链表相连 → 范围查询高效；
- 对比：哈希索引只支持等值查询；B 树非叶子存数据导致树更高、范围查询差。

### 4.2 InnoDB 索引体系

- **聚簇索引（主键）**：叶子节点 = 整行数据，表数据就长在它身上；
- **二级索引**：叶子 = 索引列 + 主键值；
- **回表**：二级索引找到主键 → 再回聚簇索引取整行；
- **覆盖索引**：查询需要的列全在索引里 → 免回表（性能提升常用手段）；
- **联合索引最左前缀**：(a,b,c) 可支撑 a / a,b / a,b,c 的组合；范围查询会中断后续列；
- **索引下推 ICP**（MySQL 5.6+）：过滤条件下推到引擎层，减少回表。

### 4.3 索引失效清单（高频踩坑）

| 写法 | 问题 | 改写 |
|---|---|---|
| `WHERE YEAR(dt)=2026` | 列上套函数 | `dt >= '2026-01-01' AND dt < '2027-01-01'` |
| `WHERE user_id = '123'`（int 列） | 隐式类型转换 | 类型匹配 |
| `LIKE '%abc'` | 前缀模糊 | 改后缀 / 全文索引 / 向量检索 |
| `a=1 OR b=2`（b 无索引） | OR 拖累 | 拆成 UNION |
| `!= / NOT IN`（大范围） | 低选择性 | 视情况换分段查询 |
| 跳过联合索引最左列 | 断链 | 调整索引顺序 |
| ORDER BY 与索引序不一致 | filesort | 让排序用上索引 |

### 4.4 EXPLAIN 解读

- 核心列：`type`（访问类型）、`key`（用到哪个索引）、`rows`（预估行数）、`filtered`（过滤比）、`Extra`（关键信号）；
- type 等级：`system > const > eq_ref > ref > range > index > ALL`，出现 ALL（全表扫描）要警觉；
- Extra 信号：`Using index`（覆盖索引，优）｜ `Using filesort`（额外排序）｜ `Using temporary`（临时表）｜ `Using index condition`（ICP）；
- 真实对比（SQLite 等价演示）：
  ```
  加索引前: SCAN orders
  加索引后: SEARCH orders USING INDEX idx_orders_user (user_id=?)
  ```

### 4.5 深分页优化（大厂高频）

```sql
-- 慢：先扫 100 万行再丢掉
SELECT * FROM t ORDER BY id LIMIT 1000000, 20;

-- 快①：延迟关联（先用覆盖索引拿主键）
SELECT t.* FROM t
JOIN (SELECT id FROM t ORDER BY id LIMIT 1000000, 20) x ON t.id = x.id;

-- 快②：游标(keyset)分页——记住上一页最后的 id
SELECT * FROM t WHERE id > 1000200 ORDER BY id LIMIT 20;
```

### 4.6 慢查询治理流程

```
监控（慢日志 / performance_schema）→ 定位 TOP SQL → EXPLAIN 分析
→ 三级手段（改索引 / 改 SQL / 改架构）→ 回归验证
```

工具：pt-query-digest、mysqld_exporter + Prometheus + Grafana。

---

## 5. 事务、锁与并发

### 5.1 ACID 的工业含义

- **A 原子性**：要么全成要么全回滚（undo log 实现）；
- **C 一致性**：约束与业务规则不被破坏（是目的，A/I/D 是手段）；
- **I 隔离性**：并发事务互不干扰（锁 + MVCC 实现）；
- **D 持久性**：提交即落盘（redo log，WAL 机制）。

### 5.2 隔离级别

| 级别 | 脏读 | 不可重复读 | 幻读 | 默认于 |
|---|---|---|---|---|
| READ UNCOMMITTED | ✗ | ✗ | ✗ | 几乎不用 |
| READ COMMITTED | ✓ | ✗ | ✗ | PG / Oracle 默认 |
| REPEATABLE READ | ✓ | ✓ | ✗（InnoDB 基本解决） | **MySQL 默认** |
| SERIALIZABLE | ✓ | ✓ | ✓ | 性能差，极少用 |

### 5.3 MVCC（多版本并发控制）

三件套：
1. 每行有隐藏列：`trx_id`（最后修改它的事务）+ `roll_pointer`（指向 undo log 中的旧版本）；
2. undo log 把旧版本串成**版本链**；
3. **ReadView**（活跃事务列表）判断"哪个版本对当前事务可见"。

- **RC**：每次 SELECT 都生成新 ReadView → 能看到别人已提交的新数据；
- **RR**：第一次 SELECT 生成后复用 → 全程快照一致；
- 快照读（普通 SELECT）走 MVCC；当前读（`FOR UPDATE`、UPDATE/DELETE）读最新版本并加锁。

### 5.4 锁体系

| 锁 | 说明 |
|---|---|
| 共享锁 S / 排他锁 X | 读锁 / 写锁 |
| 意向锁 IS/IX | 表级"意图"标志，快速判断表内有无行锁 |
| 记录锁 Record Lock | 锁住单行索引记录 |
| 间隙锁 Gap Lock | 锁索引间隙，防止插入（RR 下基本默认开启） |
| 临键锁 Next-Key Lock | 记录 + 前间隙，RR 防幻读的关键 |
| 插入意向锁 | INSERT 等待间隙时的信号锁 |

记忆点：**RC 基本关闭间隙锁**（更少死锁）；RR 默认开启（更防幻读但更易死锁）。

### 5.5 死锁：排查与预防

- 排查：`SHOW ENGINE INNODB STATUS` → LATEST DETECTED DEADLOCK 段，看两个事务的 SQL、持有的锁、等待的锁；
- 预防：固定访问顺序、小事务、缩短锁持有时间、尽量命中索引（防行锁升级全表）、必要时降为 RC、业务侧乐观锁兜底。

### 5.6 乐观锁 vs 悲观锁

- 悲观锁：`SELECT ... FOR UPDATE`，冲突激烈场景；
- 乐观锁：版本号 CAS——`UPDATE t SET version = version + 1 WHERE id = ? AND version = ?`，冲突少场景（互联网主流）。

### 5.7 分布式事务方案选型

| 方案 | 一致性 | 侵入性 | 典型场景 |
|---|---|---|---|
| 2PC / XA | 强一致 | 低 | 传统金融；性能差 |
| TCC | 最终一致 | 高 | 支付核心 |
| Saga | 最终一致 | 中 | 长流程履约（订单-库存-物流） |
| 本地消息表 / 事务消息 | 最终一致 | 低 | **大多数互联网业务** |
| Seata AT | 最终一致 | 低 | Java 生态标配 |

---

## 6. 工业架构常识

### 6.1 主从复制

- 原理：主库写 **binlog** → 从库 IO 线程拉取写 relay log → SQL 线程重放；
- binlog 三格式：STATEMENT / **ROW**（推荐，确定性重放） / MIXED；
- 复制模式：异步 / 半同步 / 组复制 MGR；
- CDC（变更数据捕获）：canal / Debezium 消费 binlog → 数据同步、缓存失效、搜索索引。

### 6.2 读写分离

写主读从；核心难题是**主从延迟**。对策：强一致场景强制走主库、半同步复制、业务上容忍读旧数据的场景才走从库。

### 6.3 分库分表

- 垂直拆分（按业务/字段）、水平拆分（按行）；
- 中间件：ShardingSphere / Vitess / 云原生分布式数据库；
- 难点：分片键设计、跨片 JOIN / 分页 / 聚合的代价、扩容（一致性哈希、双写迁移）。

### 6.4 连接池（以 HikariCP 为例）

- `maximumPoolSize` 不是越大越好（连接是稀缺资源），从 `CPU核数 × 2 + 磁盘数` 起步，压测调优；
- `connectionTimeout`、`maxLifetime`（要小于 DB 的 wait_timeout）、`leakDetectionThreshold`。

### 6.5 数据库版本管理

Flyway / Liquibase：DDL 变更脚本化、进 Git、随 CI/CD 自动执行，可审计。

---

## 7. Java 编程接入（贴合主线）

### 7.1 演进路线

```
JDBC → Spring JDBC → MyBatis → MyBatis-Plus / JPA(Hibernate)
```

- 学原理：从 JDBC 开始（一切框架的底座）；
- 做业务：MyBatis / MyBatis-Plus 国内主流；JPA 海外与标准派常用。

### 7.2 JDBC 标准流程（骨架代码）

```java
String sql = "SELECT id, name FROM users WHERE age > ?";
try (Connection conn = dataSource.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setInt(1, 18);
    try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            long id = rs.getLong("id");
            String name = rs.getString("name");
            // ...
        }
    }
}
```

要点：
- 必须用 PreparedStatement（占位符预编译，**防 SQL 注入**）；
- 批处理：`addBatch()` / `executeBatch()`；
- 事务：`conn.setAutoCommit(false)` → 业务操作 → `commit()` / 异常时 `rollback()`。

### 7.3 MyBatis 要点

- `#{}`：预编译占位符（安全，**默认都用它**）；
- `${}`：字符串拼接（有注入风险，仅用于动态表名/排序字段等，且必须白名单校验）；
- Mapper 接口 + XML 映射；复杂结果用 resultMap。

### 7.4 编程规范 checklist

- ☑ 禁止 `SELECT *`（改字段、走覆盖索引）；
- ☑ 必须分页（禁止全量捞）；
- ☑ 禁止循环里查库（N+1 问题）→ 用 IN 批量查或 JOIN；
- ☑ 大事务拆小；连接池化并正确释放；
- ☑ 写操作检查是否命中索引（把 EXPLAIN 写进 Code Review 清单）。

---

## 8. 时代背景：2025–2026 数据库在发生什么

### 8.1 格局

| 类型 | 代表 | 用途 |
|---|---|---|
| OLTP | MySQL（9.x 已迭代至 9.7）、PostgreSQL 18、Oracle | 在线交易 |
| OLAP | ClickHouse、Doris、StarRocks、DuckDB | 分析报表 |
| HTAP | TiDB、OceanBase、PolarDB | 一份数据两种负载 |
| 向量 | Milvus、Qdrant、pgvector、Redis 8 | AI 检索 |

### 8.2 向量检索进入 SQL（AI 时代的新能力）

```sql
-- pgvector：按"向量距离"排序，和普通 ORDER BY 无异（<-> 欧氏、<=> 余弦、<#> 内积）
SELECT id, content FROM docs
ORDER BY embedding <-> '[0.12, 0.34, ...]'
LIMIT 10;

-- MySQL 9.x：原生 VECTOR 类型 + STRING_TO_VECTOR()/VECTOR_DIM() 等函数
-- （DISTANCE() 距离函数随 MySQL HeatWave / MySQL AI 发行版提供）
```

> RAG 的向量检索层、Agent 的长期记忆层，正在被关系数据库"吸收"——SQL 技能在 AI 时代不降反升。

### 8.3 AI × 数据库

- **NL2SQL（Text2SQL）**：自然语言 → SQL，Agent 直连数据库查询；
- 自动索引推荐、参数自调优（云数据库自治能力）；
- LLM 应用的元数据（用户、会话、文档权限）依然靠关系库管理。

---

## 9. 学习路径与练习

### 9.1 四阶段

| 阶段 | 目标 | 过招标准 |
|---|---|---|
| 1. SQL 语法 | CRUD + JOIN + 聚合 | LeetCode 数据库简单题 20 道 |
| 2. 分析能力 | 窗口函数 + CTE | 连续登录 / 分组 TopN 能独立写出 |
| 3. 性能 | 索引 + EXPLAIN | 能解释一条慢 SQL 并给出 2 种优化方案 |
| 4. 工程 | 事务 + 架构 + Java 接入 | 用 Spring Boot + MyBatis 完成完整小项目 |

### 9.2 练习资源

- LeetCode Database（面试导向）、SQLZoo（交互式）、HackerRank SQL；
- 动手：Docker 起 MySQL 8.4 / PostgreSQL 18，导入公开数据集；
- 本工作区 `sql-demo/sql_demo.py` 可随时扩展新题目自测。

### 9.3 书单

《高性能 MySQL》《SQL 进阶教程》(MICK)《数据库系统概念》《PostgreSQL 修炼之道》

---

## 附录 A：真实运行输出节选

> 来自 `sql-demo/sql_demo.py` 实际执行（SQLite 3.45.1），语法与 MySQL/PG 基本一致。

**① 连续 3 天登录（日期-行号分组法）**

```
(1, '2026-03-01', 3)
← 用户1从 3-01 起连续登录 3 天；
  用户2只有 3-01/3-03/3-04，未连续 3 天，被正确过滤
```

**② ROW_NUMBER vs RANK（并列名次语义）**

```
('赵敏', 2, 20000.0, rn=1, rk=1)
('刘洋', 2, 20000.0, rn=2, rk=1)   ← RANK 并列同名次，ROW_NUMBER 强制不重复
```

**③ 索引效果对比（EXPLAIN）**

```
加索引前: SCAN orders
加索引后: SEARCH orders USING INDEX idx_orders_user (user_id=?)
```

完整输出见 `sql-demo/demo_output.txt`（含 JOIN、TopN、累计值、行转列、递归 CTE 等 9 组）。

## 附录 B：面试速查 10 问

1. **SQL 逻辑执行顺序？** FROM → JOIN → WHERE → GROUP BY → HAVING → SELECT → DISTINCT → ORDER BY → LIMIT。
2. **WHERE 与 HAVING 区别？** WHERE 过滤行（聚合前，能用索引）；HAVING 过滤组（聚合后）。
3. **CHAR 与 VARCHAR？** CHAR 定长可能补空格；VARCHAR 变长，N 影响内存与索引。
4. **COUNT(\*) / COUNT(1) / COUNT(列)？** 前两者统计所有行；COUNT(列) 忽略 NULL。
5. **什么是回表？如何避免？** 二级索引 → 主键 → 聚簇索引取数；覆盖索引可避免。
6. **MySQL 默认隔离级别？幻读怎么解决？** RR；快照读靠 MVCC，当前读靠 Next-Key Lock。
7. **redo log 与 binlog 区别？** redo=引擎层、崩溃恢复、循环写；binlog=Server 层、归档/复制、追加写。
8. **为什么推荐自增主键？** 小且有序 → 插入不撕裂 B+ 树；二级索引叶子更小。
9. **深分页怎么优化？** 延迟关联 / keyset 游标分页。
10. **分布式事务怎么选？** 大多数业务用本地消息表 / Seata AT（最终一致）；强一致核心用 TCC/XA。

## 附录 C：配套文件清单

- `sql-demo/sql_demo.py` — 可运行演示脚本（建议自己跑一遍，改数据玩）
- `sql-demo/demo_output.txt` — 真实运行输出存档
- 《学习报告-Java-数据库-AI-Agent.md》 — 三大方向总览