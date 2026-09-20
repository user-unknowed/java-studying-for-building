# Java EE（Jakarta EE）企业级开发 · 现代实战教程

> 面向已掌握 JavaSE 与 SQL 基础的学习者 ｜ 整理：Operit AI ｜ 2026-09
>
> 配套可运行项目（全部真实编译运行验证）：
> - `demos/jakarta-ee/library-api` —— 图书借阅管理 REST API（Servlet + JDBC + SQLite）
> - `demos/jakarta-ee/order-service` —— 迷你订单服务（含防超卖并发压测）
> - `demos/spring-boot/order-service` —— 同一订单业务的 Spring Boot 版（与原生版逐行对照）
> - `demos/spring-boot/shortlink-service` —— 短链接服务（302 跳转 + 唯一约束 + 原子计数）

---

## 0. 导读：2026 年，我们说的"Java EE"到底是什么？

如果你在网上看到这些词——**Java EE、J2EE、Jakarta EE、Jakarta EE 10 / 11**——它们说的是同一件事的不同阶段：

> **一套让 Java 能够开发"企业级后端服务"的标准规范集合。**

- "企业级" ≈ 需要处理：HTTP 请求、数据库、事务、并发、安全、消息……
- "规范"的意思是：**它只是接口标准**，由不同的厂商/开源项目提供实现（Tomcat、WildFly、Payara……）
- 2018 年，Oracle 把 Java EE 移交给了 Eclipse 基金会，并改名 **Jakarta EE**（因为 "Java" 商标属于 Oracle）

**为什么 2026 年还值得学？三个理由：**

1. **它没有死，只是换了舞台**：今天主流的 Spring Boot 应用，内部跑的仍然是 Servlet 容器（内嵌 Tomcat 就是一个 Servlet 容器）
2. **面试与排障的降维打击**：懂 Servlet 生命周期、事务边界、DAO 分层的人，看 Spring 问题一看一个准
3. **思想 100% 活在现代技术栈里**：本篇第 2 节有一张完整对照表

---

## 1. 从 J2EE 到 Jakarta EE：一次改名背后的技术史

### 1.1 时间线

| 年份 | 事件 | 关键词 |
|---|---|---|
| 1999 | J2EE 1.2 发布 | Servlet、JSP 时代开启 |
| 2006 | 改名 Java EE 5 | 注解配置、EJB 3.0 |
| 2013 | Java EE 7 | WebSocket、JSON 处理 |
| 2017 | Java EE 8 发布 | Servlet 4.0、HTTP/2 |
| 2017.9 | Oracle 将 Java EE 移交给 Eclipse 基金会 | "开源化" |
| 2018 | 正式更名 **Jakarta EE** | 品牌切割 |
| 2019 | Jakarta EE 8 —— 交接后第一个版本 | 与 Java EE 8 兼容 |
| 2020-2022 | **Jakarta EE 9 / 9.1 / 10** | ⭐ `javax.*` → `jakarta.*` 命名空间迁移 |
| 2024 | Jakarta EE 11 | Servlet 6.1、虚拟线程友好 |
| 现在 | 最新规范持续迭代 | 云原生、虚拟线程 |

### 1.2 最重要的技术断层：javax → jakarta

**这是初学者最容易被坑的点**：网上大量"老教程"的代码是 `javax.servlet.*`，而现代的 Tomcat 10+ 用的是 `jakarta.servlet.*`：

```java
// Jakarta EE 9 之前（老代码）：
import javax.servlet.http.HttpServlet;

// Jakarta EE 9+（现代代码，本教程配套项目使用的）：
import jakarta.servlet.http.HttpServlet;
```

| 组合 | 命名空间 | 例子 |
|---|---|---|
| Tomcat 9 及以下 + Java EE 8 | `javax.*` | 老项目、老教程 |
| Tomcat 10+ + Jakarta EE 10/11 | `jakarta.*` | ✅ 现代写法 |

> 记忆口诀：**"10 开始 jakarta"**——Tomcat 10+、Jakarta EE 9+ 之后，包名前缀都是 `jakarta`。

下面这份"真实启动日志"来自本项目的实际运行（终端里跑出来的，不是编的）：

```
  图书借阅管理 API 已启动 (嵌入式 Tomcat 10.1 / Servlet 6.0)
  地址: http://localhost:8081/api/books
```

---

## 2. 技术全景：Java EE 规范 vs 现代实际技术栈

一张表看清"规范"与"今天的实际工业选择"的对应关系：

| Java EE 规范 | 解决的问题 | 现代实际常用 | 备注 |
|---|---|---|---|
| **Servlet** | HTTP 请求处理 | ✅ 依然存在 | Spring MVC 底层就是 Servlet |
| JSP | 服务端页面渲染 | 前后端分离（Vue/React）+ REST | 基本淘汰 |
| **JPA** | 对象-关系映射（ORM） | Spring Data JPA / MyBatis | ORM 思想不变 |
| EJB | 分布式组件/事务 Bean | Spring Bean + @Transactional | EJB 基本消亡 |
| **JAX-RS** | REST 风格 Web 服务 | Spring MVC 注解 | 思想一脉相承 |
| CDI | 依赖注入 | Spring IoC / @Autowired | 理念完全一致 |
| Bean Validation | 参数校验 | Hibernate Validator | 规范仍在广泛使用 |
| JMS | 异步消息 | Kafka / RocketMQ / RabbitMQ | 中间件演进 |
| JTA | 分布式事务 | Spring 事务管理 / Seata | 复杂度问题 |
| JAX-WS | SOAP Web 服务 | 基本被 REST 取代 | 遗留系统 |
| WebSocket | 双向通信 | Spring WebSocket / Netty | 实时场景 |
| Security API | 认证授权 | Spring Security / JWT | 生态化 |

**结论：不要"学 Java EE 还是学 Spring Boot"二选一——正确姿势是：**

> **用 Java EE 的底层思想打地基，用 Spring Boot 的工程化提效率。**

本教程第 9 节会给你完整的迁移对照。

---

## 3. Web 层核心：Servlet（一切 Java Web 的起点）

### 3.1 HTTP 复习：一次请求发生了什么

```
浏览器/curl --HTTP请求--> [ Tomcat（Servlet 容器）] --调用--> Servlet.service()
         <--HTTP响应-- [ 线程池 / 连接管理 / 协议解析 ] <--返回-- result
```

要牢记：**Servlet 只是"接请求、还响应"的组件**；连接管理、线程池、协议解析这些脏活累活都是"容器"（Tomcat）干的。

### 3.2 Servlet 生命周期（面试高频）

| 阶段 | 方法 | 说明 |
|---|---|---|
| 加载 & 初始化 | `init()` | 容器启动时调用一次 |
| 服务 | `service()` → `doGet()/doPost()` | 每个请求调用（多线程！） |
| 销毁 | `destroy()` | 容器关闭时调用一次 |

⚠️ 因为 `service()` 是多线程并发调用，**Servlet 里不要出现可变共享状态**（本项目的 DAO 都是无状态的，天然安全）。

### 3.3 项目里怎么用 Servlet

极简三件套（嵌入式 Tomcat）：

```java
// 1. 启动容器
Tomcat tomcat = new Tomcat();
tomcat.setPort(8081);
tomcat.getConnector();

// 2. 注册 Servlet 与 URL 映射
Context ctx = tomcat.addContext("", null);
Tomcat.addServlet(ctx, "bookApi", new BookApiServlet());
ctx.addServletMappingDecoded("/api/books/*", "bookApi");

// 3. 启动，阻塞等待请求
tomcat.start();
tomcat.getServer().await();
```

URL 中 `*` 是通配符，`req.getPathInfo()` 拿到的是映射之后的剩余路径（如 `/3/borrow`），据此做路由分发——这就是"手写 REST 路由"的核心。

---

## 4. 数据访问层：DAO 分层与事务边界

### 4.1 分层职责（现代架构的最小单元）

```
Servlet（Web 层）   ← 只管 HTTP：解析参数、设置状态码、拼 JSON
   ↓ 调用
DAO（数据层）       ← 只管数据：SQL、事务、对象映射
   ↓ 调用
Db（基础设施）      ← 连接管理（相当于 Spring 里 DataSource 的雏形）
```

**边界原则**：
- SQL 只出现在 DAO 里；
- HTTP 状态码只在 Servlet 里；
- 任何一层都不"越权"。

### 4.2 事务边界放在哪？

放 DAO 的"业务方法"上（如 `borrow()`、`place()`、`cancel()`），一个方法 = 一个原子业务操作：

```java
public String borrow(long bookId, String borrower) throws Exception {
    try (Connection c = Db.get()) {
        c.setAutoCommit(false);      // 开启事务
        try {
            // ① 扣库存（条件更新）
            // ② 写借阅记录
            c.commit();              // 全部成功 → 提交
            return "OK";
        } catch (Exception e) {
            c.rollback();            // 任何失败 → 全部回滚
            throw e;
        }
    }
}
```

> 你以后学 Spring 时看到的 `@Transactional`，做的事情和这段"手工模板"一模一样——只是框架用 AOP 帮你自动包上了。

---

## 5. REST API 设计实践（工业规范）

### 5.1 资源与动词

| 动词 | 语义 | 幂等？ | 例子 |
|---|---|---|---|
| GET | 查询 | ✅ | `GET /api/books/1` |
| POST | 创建/动作 | ❌ | `POST /api/orders` |
| PUT | 全量更新 | ✅ | （本项目未用） |
| DELETE | 删除 | ✅ | （本项目未用） |

### 5.2 状态码语义（本项目真实用到的）

| 状态码 | 含义 | 项目中的场景 |
|---|---|---|
| 200 OK | 成功 | 查询、借书、还书、取消成功 |
| 201 Created | 创建成功 | 新增图书、下单成功 |
| 400 Bad Request | 参数错误 | 缺字段、id 非法 |
| 404 Not Found | 资源不存在 | 查询不存在的书/订单 |
| 409 Conflict | 业务冲突 | 库存不足、重复取消 |
| 500 Internal Server Error | 服务端错误 | 未预期异常 |

### 5.3 错误响应格式

统一成 `{"error": "..."}`，前端可以稳定解析：

```json
{"error":"库存不足，借阅失败"}
```

### 5.4 幂等性：一个高级话题的第一次接触

"取消订单"这个动作被点两次会发生什么？如果代码写得随意，库存会被回滚两次——**灾难**。本项目用"状态守卫"解决（第 8 节讲）。

> 幂等 = 同一个请求执行一次和执行多次，对系统状态的最终影响相同。支付回调、取消、重试场景的核心议题。

## 6. 并发控制实战：一次真实压测揭示的"超卖"真相

### 6.1 复现现场：15 个并发抢 3 台显示器

为项目预置的商品中，「4K显示器」库存 = 3。用 `stress_test.py` 模拟 15 个用户同时下单，每个订单只买 1 台：

```bash
python3 stress_test.py 15 3 1     # 15 个并发 · 目标商品 id=3 · 每单 1 件
```

真实运行结果（`run_output.txt` 原文摘录）：

```
===== 7. 并发压测：15 个并发抢购 3 台 4K显示器（防超卖验证） =====
并发发起 15 个订单（商品 id=3，每单 1 件）...
成功下单(201): 3 | 库存不足(409): 12 | 其他错误: 0
压测后商品[4K显示器]剩余库存: 0
结论: 成功单数 = min(初始库存/每单数量, 并发数)，库存 >= 0，未发生超卖 ✓
```

**3 人抢到，12 人拿到 409 拒绝，库存恰好归零——没有一件超卖。**

### 6.2 如果写成"先查再扣"，灾难就来了

面试最常考的反例（天真写法）：

```java
// ❌ 危险：检查和扣减是两条独立 SQL，之间存在"时间窗口"
Product p = dao.findById(id);           // 读到 stock = 1
if (p.stock >= qty) {                   // 判断：1 >= 1 ✓
    dao.updateStock(id, p.stock - qty); // 写回 stock = 0
}
```

两个并发请求交错时：

```
请求A: 读到 stock=1 ─────────────► 判断通过 ──► 写回 stock = 1-1 = 0
请求B:       读到 stock=1 ─────────────► 判断通过 ──► 写回 stock = 1-1 = 0
              ▲ 两个请求读到了同一个旧值，各自"算好再写回"
```

结果：**2 笔订单成交，库存却只减了 1 件 → 超卖 1 件。**

问题本质：**"读—判断—写"被拆成了多个步骤，而数据库只保证单条语句内部的原子性。**

### 6.3 正解：把判断压进 UPDATE 的 WHERE 里

本项目 `OrderDao.place()` 内部的防超卖核心（源码原文）：

```java
// ① 条件扣减库存（原子操作，防超卖的关键）
try (PreparedStatement ps = c.prepareStatement(
        "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?")) {
    ps.setInt(1, it.quantity);
    ps.setLong(2, it.productId);
    ps.setInt(3, it.quantity);
    if (ps.executeUpdate() == 0) {
        // 影响 0 行 = 条件不成立：区分"不存在"(404) 与"库存不足"(409)
        throw new BizException(
                exists ? 409 : 404,
                exists ? "库存不足: productId=" + it.productId
                       : "商品不存在: productId=" + it.productId);
    }
}
```

三个关键点：

1. **`WHERE ... AND stock >= ?`**：库存够不够，交给数据库在执行更新那一刻判断——检查与扣减在同一条语句内原子完成，没有时间窗口；
2. **`executeUpdate() == 0`**：影响 0 行 = 条件不成立 = 库存不足，直接抛业务异常 → HTTP 409；
3. **再查一次"是否存在"**：给调用方准确的原因（409 库存不足 / 404 不存在），这是好 API 的细节。

### 6.4 这是"乐观"策略——那"悲观"呢？

| 策略 | 手段 | 特点 |
|---|---|---|
| **乐观**（本项目） | 条件 UPDATE + 检查影响行数 | 无锁开销；冲突时快速失败（拒绝或重试） |
| **悲观** | `SELECT ... FOR UPDATE` 提前锁行 | 万无一失，但持锁期间其他请求全要等待，高并发吞吐差 |

秒杀、抢票、库存扣减这类"写冲突剧烈"的场合，工业界主流恰恰是**乐观思路 + 数据库原子性**（条件更新、版本号 CAS），而不是把人锁在门外。

### 6.5 小结

> SQL 不只是"存取数据"的工具。把业务规则（"库存必须够"）翻译成 SQL 的**条件**（`stock >= ?`），就免费得到了一份数据库级别的并发保护——这是 SQL 最被低估的能力之一。

---

## 7. 事务：把"要么全成、要么全不成"写进代码

### 7.1 为什么必须有事务

「下单」= 扣库存 + 建订单 + 写明细。如果扣完库存之后程序崩了、订单没建成——钱货两空。

> 事务 = 一组操作的"最小不可分单元"：**要么全部生效，要么全部不生效（回滚）。**

数据库事务的四大保证（ACID，面试必背但更要理解）：

| 特性 | 含义 | 靠什么实现 |
|---|---|---|
| **原子性** Atomicity | 全成或全不成 | undo 日志 / rollback |
| **一致性** Consistency | 约束与业务规则不被破坏 | 约束 + 正确的业务代码 |
| **隔离性** Isolation | 并发事务互不干扰 | 锁 / MVCC |
| **持久性** Durability | 提交后不丢 | 日志（SQLite 的 WAL 就是它的一种） |

### 7.2 手动挡：原生版的完整事务模板

本项目 library-api 的借书操作（`BookDao.borrow`，节选）：

```java
public String borrow(long bookId, String borrower) throws Exception {
    try (Connection c = Db.get()) {
        c.setAutoCommit(false);        // ★ 关闭自动提交 = 开启事务
        try {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE books SET stock = stock - 1 WHERE id = ? AND stock > 0")) {
                ps.setLong(1, bookId);
                if (ps.executeUpdate() == 0) {
                    c.rollback();      // 扣不动：库存不足或书不存在
                    return exists ? "NO_STOCK" : "NOT_FOUND";
                }
            }
            // ② 写入借阅记录（INSERT INTO borrow_records ...）
            c.commit();                // 全部成功 → 提交
            return "OK";
        } catch (Exception e) {
            c.rollback();              // 任何失败 → 回滚
            throw e;
        }
    }
}
```

五步口诀：**关自动提交 → 干活 → commit → 出错 rollback → try-with-resources 自动关连接。**

> 你以后学 Spring 时看到的 `@Transactional`，做的事情和这个模板一模一样：方法正常返回 → commit；抛异常 → rollback。框架只是用 AOP 帮你把模板"包"了起来（第 9 章有对照）。

### 7.3 回滚不是"撤销打印"，而是数据库的真正回退

- commit 之前，所有改动都躺在事务日志里"待命"，其他连接看不到（隔离性）；
- rollback 把待命改动整组丢弃，数据回到事务开始前的状态；
- 所以"先扣库存、再建订单、再写明细"三步中任何一步失败，前两步都会被完整撤销。order-service 实测中「超量下单被拒绝」之后库存纹丝不动，正是这个机制在工作。

### 7.4 隔离级别：并发事务的"交通规则"

| 级别 | 脏读 | 不可重复读 | 幻读 | 谁在用 |
|---|---|---|---|---|
| READ UNCOMMITTED | ✅ 可能 | ✅ | ✅ | 几乎不用 |
| READ COMMITTED | ❌ | ✅ | ✅ | PostgreSQL / Oracle 默认 |
| REPEATABLE READ | ❌ | ❌ | ✅* | MySQL InnoDB 默认（*间隙锁基本解决） |
| SERIALIZABLE | ❌ | ❌ | ❌ | 最严，性能开销最大 |

三个名词一句话解释：

- **脏读**：读到了别人"未提交"的数据——别人一回头回滚了，你读到的就是鬼影；
- **不可重复读**：同一事务里两次读同一行，值被别人改了；
- **幻读**：同一事务里两次范围查询，凭空多出（或少掉）了行。

> 本项目用的 SQLite：它的**写操作是全库串行的**（同一时刻只允许一个写事务）——简单粗暴，但天然没有脏读/幻读问题。

### 7.5 SQLite 的并发细节：连接串里的两个参数

原生版连接串（`Db.java` 原文）：

```java
private static final String URL =
        "jdbc:sqlite:order.db?journal_mode=WAL&busy_timeout=5000";
```

- **WAL**（Write-Ahead Logging）：写操作先追加到日志文件，**读不再被写阻塞**——读写可以并行；
- **busy_timeout=5000**：遇到写锁被占用时，最多等 5 秒再报错，而不是立刻抛 `SQLITE_BUSY`。高并发下它把"直接失败"变成了"稍等重试"。

对照工业级数据库：MySQL / PostgreSQL 是行级锁 + MVCC，思想一致（写者互斥、读者不挡），只是粒度细得多。**你在 SQLite 上学到的并发概念，换个数据库全部适用。**

### 7.6 事务边界的三条纪律（真实项目最常犯）

1. **事务要短**：commit 之前锁可能被占着——不要在事务里做网络调用（发短信、调支付接口）；
2. **边界放业务方法**：一个业务操作一个事务（本项目的 `place` / `cancel` / `borrow`），而不是"每个 SQL 各自 commit"；
3. **异常必须炸出去**：吞掉异常 = 框架/上层无法感知失败、无法回滚。正确姿势：抛自定义业务异常，交给上层翻译成 HTTP 状态码。

---

## 8. 幂等性工程：取消订单背后的"状态守卫"

### 8.1 重复请求从哪里来（不是 bug，是常态）

- 用户手抖，把"取消订单"点两次；
- 网络超时后客户端自动重试（客户端根本不知道上一次到底成功没有）；
- 消息队列重投递、支付回调重发……

> **幂等 = 同一操作执行一次与执行多次，对系统最终状态的影响相同。**

对"取消订单"来说：第一次把订单 CREATED → CANCELED 并回滚库存；第二次必须做到"什么都不影响"——最差也必须拒绝，绝不能把库存再加一遍。

### 8.2 反例：库存被回滚两次会怎样

假设 cancel 只是两条无条件的 UPDATE：

```sql
UPDATE orders   SET status = 'CANCELED' WHERE id = ?;
UPDATE products SET stock  = stock + ?  WHERE id = ?;   -- 危险
```

点两次 → 库存加了两次 → 凭空多货。若关联退款，就是真金白银的资损。

### 8.3 正解：状态守卫（condition-guarded transition）

`OrderDao.cancel()` 的核心（源码原文）：

```java
// ① 状态守卫：只有 CREATED → CANCELED 的这一次更新能成功
try (PreparedStatement ps = c.prepareStatement(
        "UPDATE orders SET status = 'CANCELED' WHERE id = ? AND status = 'CREATED'")) {
    ps.setLong(1, orderId);
    if (ps.executeUpdate() == 0) {
        // 影响 0 行：要么订单不存在，要么已经不是 CREATED 状态
        throw new BizException(exists ? 409 : 404,
                exists ? "订单当前状态不可取消（可能已取消）" : "订单不存在: id=" + orderId);
    }
}
// ② 库存回滚（能执行到这里，说明这一次状态迁移刚刚由"我"完成）
```

真实实测（`run_output.txt` 原文）：

```
===== 4. 取消订单2 → 库存回滚（应为 2+2=4） =====
HTTP 200
----- 键盘库存 -----
{"id":1,"name":"机械键盘","price":399.0,"stock":4}

===== 5. 重复取消订单2 → 应拒绝（409） =====
{"error":"订单当前状态不可取消（可能已取消）"}
HTTP 409
```

第一次：200，库存 2 → 4；第二次：409，库存保持 4。**恰好一次生效。**

### 8.4 为什么这条 SQL 是幂等的关键

把订单想成状态机：

```
CREATED ──cancel──► CANCELED
   ▲
   └── 重复 cancel 在这里被拦截（没有合法出边）
```

- `WHERE status = 'CREATED'` 让这条 UPDATE **只可能成功一次**——第二次执行时条件不成立，影响 0 行；
- 库存回滚放在"守卫通过之后"，所以永远不会执行两次；
- 这就是「状态机 + 条件更新」的工程实现：业务规则（"只有未取消的订单能取消"）被直接翻译成了 SQL 条件。

### 8.5 幂等性工具箱（按场景选用）

| 手段 | 适用场景 | 例子 |
|---|---|---|
| **状态守卫**（本项目） | 有明确状态机的操作 | 取消订单、支付回调置"已支付" |
| **幂等键** | 创建类操作 | 支付请求携带 `requestId`，重复请求返回原结果 |
| **唯一约束** | 业务上天然唯一 | 短链接 code 唯一（第 9 章项目） |
| **去重表** | 消息消费 | 消费前先插消费记录，主键冲突 = 已处理过 |

> 一个自检标准：**"这个接口被调用两次，第二次会发生什么？"**——能明确回答，说明你设计过幂等；答案是"可能多扣/不知道"，那就等着线上出事故。

### 8.6 小结

> 并发控制（第 6 章）解决"同时来"；事务（第 7 章）解决"中途挂"；幂等（本章）解决"重复来"。**这三板斧构成后端写操作安全性的完整防线**，在任何语言、任何框架里都成立。

---

## 9. 从 Jakarta EE 到 Spring Boot：一次完整迁移对照

### 9.1 同一个业务，两次实现

javaee 分区里放着一对"镜像项目"：

- `javaee/demos/jakarta-ee/order-service` —— 原生 Servlet + 手写路由 + 手动事务（**看懂原理**）
- `javaee/demos/spring-boot/order-service` —— Spring Boot + 注解 + 声明式事务（**现代工程**）

同一个"下单防超卖"业务，看两版代码如何逐项对应。

### 9.2 全景对照表

| 关注点 | 原生版（手写） | Spring Boot 版（框架） |
|---|---|---|
| 启动 | `new Tomcat()` + 手工注册 Servlet | `SpringApplication.run()` 一行 |
| 路由 | `getPathInfo()` + if/else 分发 | `@GetMapping` / `@PostMapping` 注解 |
| 组件装配 | 手动 `new OrderDao()` | 构造器注入（IoC 容器自动装配） |
| 事务 | `setAutoCommit(false)` + commit/rollback | `@Transactional` 一行注解 |
| 请求体 | `readBody()` 手动解析 JSON | `@RequestBody` 自动反序列化 |
| 参数校验 | 手写 if-null 检查 | `@Valid` + `@NotBlank` 声明式 |
| 错误处理 | 每个 Servlet try-catch 拼错误 JSON | `@RestControllerAdvice` 全局统一 |
| JSON 序列化 | Gson 手动序列化 | Jackson 自动完成 |
| 数据访问 | JDBC PreparedStatement 手写 | JdbcTemplate（仍是手写 SQL，更省心） |

### 9.3 代码级对照（挑三处最有感觉的）

**① 路由：手写分发 vs 注解声明**

原生版（`OrderApiServlet`，源码原文）：

```java
String[] seg = path.split("/"); // ["", "5", "cancel"]
if (seg.length == 3 && seg[2].equals("cancel")) {
    long id = Long.parseLong(seg[1]);
    write(resp, 200, dao.cancel(id));
    return;
}
write(resp, 400, Map.of("error", "不支持的路径: " + path));
```

Spring Boot 版（`ShopController`，源码原文）：

```java
@PostMapping("/orders")
@ResponseStatus(HttpStatus.CREATED)
public Map<String, Object> placeOrder(@Valid @RequestBody CreateOrderRequest req) {
    long orderId = orderService.placeOrder(req);
    return Map.of("orderId", orderId, "status", "PAID");
}
```

> 同样的"解析 → 分发 → 调用 → 返回"，一个写 20 行、还容易漏 catch；一个声明 5 行、校验自动。

**② 事务：手动模板 vs 一行注解**

原生版在 7.2 节已经看过（setAutoCommit + commit/rollback 全套手工）；Spring Boot 版：

```java
@Transactional
public long placeOrder(CreateOrderRequest req) { ... }
```

> `@Transactional` = "7.2 节的手工模板 + AOP 代理"。**你理解了模板，注解就只是语法糖**——而且出了事务问题，你能猜到它内部在做什么。

**③ 错误处理：每个方法一排 catch vs 全局一处**

原生版——每个 Servlet 方法尾部都要这样收尾：

```java
} catch (OrderDao.BizException e) {
    write(resp, e.httpCode, Map.of("error", e.getMessage()));
} catch (NullPointerException | IllegalStateException e) {
    write(resp, 400, Map.of("error", "请求体缺少必要字段或格式错误"));
} catch (NumberFormatException e) { ... }
```

Spring Boot 版——全项目只写一处（`ApiExceptionHandler`）：

```java
@ExceptionHandler(BizException.class)
public ResponseEntity<Map<String, Object>> biz(BizException e) {
    return ResponseEntity.status(e.status())
            .body(Map.of("error", e.getMessage(), "type", "BIZ_ERROR"));
}
```

### 9.4 Spring Boot 版的真实启动日志（server.log 原文，节选）

```
:: Spring Boot ::               (v3.5.16)
Tomcat initialized with port 8081 (http)
Starting Servlet engine: [Apache Tomcat/10.1.55]
HikariPool-1 - Start completed.
Tomcat started on port 8081 (http) with context path '/'
Started OrderServiceApplication in 5.867 seconds (process running for 7.004)
```

> 注意第 2、3、5 行：**Spring Boot 内部还是那台 Tomcat、那个 Servlet 引擎**。它不是替代品，而是"配置与装配的自动化"。你在第 3 章学的 Servlet 生命周期、第 4 章学的 DAO 分层，在这里一字不改地适用。

### 9.5 第二个现代案例：shortlink-service（短链接服务）

`javaee/demos/spring-boot/shortlink-service`：把长网址压缩成短码，访问短链时 302 跳转 + 点击统计。麻雀虽小，五脏俱全——HTTP 状态码语义、唯一约束、并发计数全都涉及。

真实实测（`run_output.txt` 原文摘录）：

```
{"originalUrl":"https://github.com/.../tree/main/java","shortUrl":"http://localhost:8083/YEUHkzQ","code":"YEUHkzQ"}

HTTP/1.1 302
Location: https://github.com/.../tree/main/java

{"id":5,"code":"YEUHkzQ","originalUrl":"https://github.com/...","clicks":3,...}   ← 访问 3 次后
```

三个设计点（都可以在源码里找到注释）：

1. **302 而不是 301**：301 会被浏览器永久缓存，后续访问不再回源，点击统计失真；
2. **`UNIQUE(code)` 约束兜底**：随机码撞车的概率极低，但绝不假设"不会发生"（生成失败自动重试）；
3. **`UPDATE link SET clicks = clicks + 1`**：单条 SQL 原子计数，避免"读-改-写"的丢失更新。

### 9.6 所以，学哪个？

- **新项目**：直接 Spring Boot，效率碾压；
- **读源码 / 排障 / 面试**：靠底层功底（Servlet 生命周期、事务边界、连接管理）；
- **学习顺序**：先用原生理解"为什么"，再用 Spring Boot 享受"怎么快"——本教程的顺序正是如此。

---

## 10. 工程化收口：构建、测试与真实踩坑记录

### 10.1 从 javac 到 Maven：工程构建的进化

原生项目用的是最朴素的方式（自己下载 jar → `javac -cp` → `java -cp`），依赖一目了然，但依赖一多就累。Spring Boot 版一条命令搞定：

```bash
mvn -q -DskipTests package     # 编译 + 打成一个可执行 fat jar
java -jar target/order-service-1.0.0.jar
```

Maven 替我们做了：依赖下载与传递、编译、打包、版本统一管理（`parent` POM）。

> `pom.xml` = 项目的"说明书"：我依赖谁、用什么版本、怎么打包，全写在里面。

### 10.2 测试策略：把证据留下来

本教程所有项目的验证都用同一套"土办法"，但它极其有效：

```bash
bash test_api.sh | tee run_output.txt      # ① 每条接口真实调用 + 真实状态码，输出存档
python3 stress_test.py 15 3 1              # ② 多线程真并发，输出存档
```

> 教程里出现的每一段"实测输出"，都来自这些文件——**用数据说话，而不是"应该能跑"**。

### 10.3 真实踩坑记录（三个坑都踩在本教程开发过程中）

**坑 1：嵌入式 Tomcat 启动看似正常，所有请求却返回 HTTP 000**

- 现象：服务日志显示已启动，但所有接口连接被重置（curl 报告 `HTTP 000`）；
- 排查：打开 `server.log` → `NoClassDefFoundError: jakarta/annotation/Resource`；
- 原因：Tomcat 10.1 的 `tomcat-embed-core` 不自带 `jakarta.annotation-api`（它假设身处完整容器）；
- 解决：把 `jakarta.annotation-api-2.1.1.jar` 加入 classpath。

> 教训：**接口全挂先看日志，别猜代码。**

**坑 2：Gson 把 `=` 序列化成 `\u003d`**

- 现象：错误响应变成 `{"error":"库存不足: productId\u003d1"}`；
- 原因：Gson 默认开启 HTML 转义（防注入），`=`、`<`、`>` 都会被转义；
- 解决：`new GsonBuilder().disableHtmlEscaping().create()`。

> 教训：了解工具的默认行为，才能避免"看起来像天书"的输出。

**坑 3（重点）：SQLite 高并发下的 12 个 500 —— 一次完整的排障现场**

背景：Spring Boot 版开发完成、单线程自测全绿后，进行 15 并发压测——

```
成功下单(201): 3 | 库存不足(400): 0 | 其他错误: 12      ← 不对劲！
```

**第 1 步 · 看日志定性**（排障永远从日志开始）：

```
[SQLITE_BUSY] The database file is locked (database is locked)
```

12 条全是 `SQLITE_BUSY`：SQLite 同一时刻只允许一个写事务，其余写操作直接失败。

**第 2 步 · 第一轮修复：连接串加参数**（与原生版看齐）：

```yaml
url: jdbc:sqlite:data/order.db?journal_mode=WAL&busy_timeout=5000
```

WAL（读写并行）+ busy_timeout（写锁冲突时等待 5 秒）。重建、重启、再压测——

```
成功下单(201): 3 | 库存不足(400): 1 | 其他错误: 11      ← 还是失败！
```

**第 3 步 · 定位真正根因："先读后写"的快照冲突**

看 `placeOrder` 的语句顺序：先 `SELECT`（读商品）再 `UPDATE`（扣库存）。

WAL 模式下，事务以**读快照**开始；期间若其他写事务提交，本事务再想升级为写操作时，快照已过期，直接失败——**busy_timeout 对锁等待有效，但对"快照过期"救不了**（等再久快照也不会变新）。

**第 4 步 · 终极修复：让写操作先行**（与原生版语句顺序对齐）：

```java
// 先 UPDATE 扣库存（写操作先行），再 SELECT 读单价
int affected = products.decreaseStock(it.productId(), it.quantity());
```

重建、重启、再压测——

```
成功下单(201): 3 | 库存不足(400): 12 | 其他错误: 0      ✅
busy_count = 0
```

**这个坑的宝贵之处**：同样的业务逻辑，"语句顺序"这样一个细节就能决定高并发下的生死。而原生版恰好一直是"写先行"（它的 `place()` 从条件扣减开始），所以从未踩到。**先学原理再上框架，就是这种时刻在帮你兜底。**

> 复盘三句话：
> 1. 并发问题只有**压测**才会暴露（单线程自测永远全绿）；
> 2. 排障闭环 = 日志定位 → 假设 → 修复 → **再压测验证**（用数据说话）；
> 3. 数据库的并发语义（锁、快照、隔离级别）值得真正理解——换 MySQL / PostgreSQL 时，这些知识全部迁移。

### 10.4 上线前自查清单（写操作安全版）

- [ ] 防超卖/防超扣：条件更新 + 检查影响行数；
- [ ] 事务边界：业务方法粒度、异常能触发回滚；
- [ ] 幂等：重复请求被拦截（状态守卫 / 唯一约束 / 幂等键）；
- [ ] 并发参数：连接池大小、WAL / busy_timeout（SQLite）、隔离级别（MySQL/PG）；
- [ ] 压测证据：并发脚本 + 输出存档；
- [ ] 错误响应：4xx / 5xx 语义清晰，日志可定位。

---

## 11. 收官：四个项目全家福与下一站路线

到这里，0~10 章已经把"企业级开发"的全部核心概念走完了：从 Servlet 原理，到事务、并发、幂等，再到 Spring Boot 迁移与工程化排障。最后一章做三件事：**把四个项目跑起来 → 回看知识地图 → 拿到进阶路线。**

### 11.1 四个项目全家福（启动速查）

> 以下路径均相对本分区根目录（`javaee/`）。

| 项目 | 目录 | 端口 | 一句话 |
|---|---|:---:|---|
| library-api | `demos/jakarta-ee/library-api` | 8081 | 图书借阅：Servlet 路由 + DAO 分层 + 事务借还 |
| order-service（原生） | `demos/jakarta-ee/order-service` | 8082 | 迷你订单：防超卖 + 状态守卫 + 并发压测 |
| order-service（Spring Boot） | `demos/spring-boot/order-service` | 8081 | 同业务现代实现：注解 + IoC + 声明式事务 |
| shortlink-service | `demos/spring-boot/shortlink-service` | 8083 | 短链接：302 跳转 + 唯一约束 + 原子计数 |

> ⚠️ 两个 order-service 都用 8081（它们本来就是"同一业务、两版实现"）——对照学习时**不要同时启动**。

**① 原生版（纯 javac，先懂原理）**

```bash
cd demos/jakarta-ee/order-service
bash download-deps.sh                      # 首次：下载依赖 jar 到 lib/
javac -encoding UTF-8 -cp "lib/*" -d out src/com/demo/order/*.java
java -Dfile.encoding=UTF-8 -cp "out:lib/*" com.demo.order.Main    # 端口 8082
```

```bash
# 另开一个终端：
bash test_api.sh | tee run_output.txt      # 冒烟测试 + 完整业务演示
python3 stress_test.py 15 3 1              # 15 并发抢 3 件库存，验证防超卖
```

library-api 同理，把 `order` 换成 `library`（端口 8081）即可，详见项目内的 README。

**② Spring Boot 版（现代工程，再求效率）**

```bash
cd demos/spring-boot/order-service
mvn -q -DskipTests package
java -jar target/order-service-1.0.0.jar   # 端口 8081
```

```bash
# 另开一个终端：
bash test_api.sh | tee run_output.txt
python3 stress_test.py 15 3 1
```

```bash
# 短链接服务（端口 8083）：
cd demos/spring-boot/shortlink-service
mvn -q -DskipTests package
java -jar target/shortlink-service-1.0.0.jar
bash test_api.sh | tee run_output.txt
```

### 11.2 知识地图：你现在会了什么

| 章节 | 如果你真的读懂了 | 对应代码位置 |
|:---:|---|---|
| 0~1 | Java EE / Jakarta EE 是什么、从哪来 | — |
| 2 | 规范 vs 现代技术栈全景对照 | — |
| 3 | Servlet 生命周期、路由与 JSON 响应 | `BookApiServlet` / `OrderApiServlet` |
| 4 | DAO 分层、事务边界、连接管理 | `BookDao.borrow()` 手写事务模板 |
| 5 | REST 语义：201 / 400 / 404 / 409 | 四个项目的接口层 |
| 6 | 并发防超卖：条件更新 + 影响行数检查 | `decreaseStock` 条件 UPDATE |
| 7 | 事务 ACID、隔离级别、SQLite 并发参数 | `place` / `cancel` / WAL + busy_timeout |
| 8 | 幂等：状态守卫防重复操作 | `cancel()` 的 `WHERE status='CREATED'` |
| 9 | Jakarta EE → Spring Boot 逐行迁移 | 两版 order-service 对照阅读 |
| 10 | 工程化：Maven、测试脚本、踩坑排障 | `application.yml` 并发参数 + 实测输出存档 |

### 11.3 下一站：五条进阶路线

> 原则：**从你手里已有的代码出发**，不要另起炉灶。

| 方向 | 为什么现在学 | 从哪行代码接上 |
|---|---|---|
| MyBatis / JPA | 手写 JDBC 的规模化替代 | 把 `BookDao` 的手写 SQL 换成 Mapper，体会"映射框架省了什么" |
| Spring Security + JWT | 真实系统必须有认证授权 | 给四个项目加登录，把"裸奔 API"变成有门禁的系统 |
| Redis | 缓存与原子操作的第二条路 | 用 Redis 原子扣减 / 分布式锁，与条件更新对比 |
| 消息队列（RabbitMQ / Kafka） | 解耦与削峰 | 下单成功后异步发通知，把同步链路拆成生产者/消费者 |
| Docker + CI/CD | 部署标准化 | 给 Spring Boot 项目写 Dockerfile，让环境问题成为历史 |

再往远看还有 **分布式事务、Spring Cloud 微服务**——建议等上面几样真正用进项目之后再出发，不急。

### 11.4 给"动手派"的三个建议

1. **改坏它**：把 `decreaseStock` 条件里的 `AND stock >= ?` 删掉，重新压测——亲眼看到超卖发生，这个知识就再也忘不掉了；
2. **看日志**：任何"结果不对劲"，先读日志再改代码（第 10 章 `SQLITE_BUSY` 就是四步排障的范例：错误分类 → 定位根因 → 修复 → **再压测验证**）；
3. **用 Git 管理实验**：每改一个点提交一次，出错时能精确回到"上一次能跑"的状态。

---

**从 Servlet 到 Spring Boot，从单线程到 15 并发，从"能跑"到"敢上线"**——这四个项目就是你继续前进的全部地基。

> 📌 配套项目的完整命令与导航，见 [JavaEE 分区 README](../README.md)。
> —— 教程全文完 ——