# order-service（Spring Boot 版）—— 迷你订单服务

> Java EE 教学项目 ①·现代工程篇 ｜ 与 [原生版 order-service](../../jakarta-ee/order-service/)（手写 Servlet）同一业务、两种实现，对照学习。

## 业务场景

电商下单闭环：商品 → 下单（扣库存）→ 订单详情 → 销售统计。

- 核心接口 `POST /api/orders`：条件扣库存 + 建订单 + 写明细，由 `@Transactional` 保证原子完成；
- 防超卖：`UPDATE product SET stock = stock - ? WHERE id = ? AND stock >= ?`；
- 并发验证：15 并发抢 3 件 → 3 成功、12 拒绝、0 超卖（见 `stress_output.txt`）。

## 运行方式

```bash
# 1. 打包（首次会下载依赖）
mvn -q -DskipTests package

# 2. 启动（8081 端口）
java -jar target/order-service-1.0.0.jar

# 3. 另一个终端：业务接口测试
bash test_api.sh | tee run_output.txt

# 4. 并发压测（15 并发 · 初始库存 3 · 每单 1 件）
python3 stress_test.py 15 3 1
```

## API 一览

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/products` | 新增商品 |
| GET | `/api/products` | 商品列表 |
| POST | `/api/orders` | 下单（核心） |
| GET | `/api/orders/{id}` | 订单详情（含明细） |
| GET | `/api/stats/sales` | 销售排行（JOIN + GROUP BY） |
| GET | `/api/stats/daily` | 每日销售额统计 |

## 值得看的三个细节

1. **`@Transactional`**：与原生版手写 `commit/rollback` 完全等价（教程第 7、9 章）；
2. **写操作先行**：`placeOrder` 里"先 UPDATE 扣库存、后 SELECT 读单价"——SQLite WAL 下的并发要点，全流程踩坑记录见教程第 10 章；
3. **连接串参数**：`application.yml` 里 `journal_mode=WAL&busy_timeout=5000`，高并发行为的关键。

---
*配套教程：`javaee/docs/05-JavaEE企业级开发-现代实战教程.md`（第 9 章有完整迁移对照）*