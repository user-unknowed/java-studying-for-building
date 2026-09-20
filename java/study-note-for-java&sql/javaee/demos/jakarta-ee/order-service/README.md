# order-service —— 迷你订单服务（防超卖实战）

真实可运行的订单系统最小闭环：**下单 → 扣库存 → 订单查询 → 取消回滚**，
重点演示 **事务 + 并发控制** 在现代业务系统里的核心地位。

## API 一览

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/products` | 商品列表 |
| GET | `/api/products/{id}` | 商品详情 |
| POST | `/api/orders` | 下单 `{"userId":"u01","items":[{"productId":1,"quantity":2}]}` |
| GET | `/api/orders/{id}` | 订单详情（含明细 JOIN） |
| POST | `/api/orders/{id}/cancel` | 取消订单（库存回滚） |
| GET | `/api/stats/sales` | 销量统计（剔除取消单） |
| GET | `/api/stats/summary` | 订单概况 |

## 快速开始

```bash
# 1. 下载依赖 jar（Tomcat + Gson + SQLite JDBC）
bash download-deps.sh

# 2. 编译
javac -encoding UTF-8 -cp "lib/*" -d out src/com/demo/order/*.java

# 3. 启动服务（端口 8082，首次启动自动插入 3 件示例商品）
java -Dfile.encoding=UTF-8 -cp "out:lib/*" com.demo.order.Main

# 4. 另开终端：跑完整测试序列 + 并发压测（并自动存档输出）
bash test_api.sh | tee run_output.txt
```

## 核心看点一：为什么不会超卖？

```sql
-- 扣减与判断在一条 SQL 内原子完成，数据库层面保证"库存足够才扣"
UPDATE products SET stock = stock - 2 WHERE id = 1 AND stock >= 2;
```

- 并发 15 个请求抢 3 件库存 → 最多 3 个成功，其余全部 409
- 实测见 `run_output.txt`（压测脚本：`stress_test.py`）

## 核心看点二：取消订单的双重保险

- **状态守卫**：`UPDATE orders SET status='CANCELED' WHERE id=? AND status='CREATED'`
  —— 只有第一次取消能成功（防止重复取消导致库存被回滚两次）
- **同事务**：状态变更与库存回滚在一个事务里，要么一起成功、要么一起失败

## 代码结构

| 文件 | 职责 |
|---|---|
| `Main.java` | 嵌入式 Tomcat 启动 + 3 个 Servlet 注册 |
| `OrderApiServlet.java` | 订单接口（下单/详情/取消） |
| `ProductApiServlet.java` | 商品接口 |
| `StatsApiServlet.java` | 统计接口（JOIN + CASE WHEN 聚合） |
| `OrderDao.java` | 订单事务核心（place / cancel） |
| `ProductDao.java` | 商品数据访问 |
| `Db.java` | 连接管理 + 建表 + 种子数据 |
| `stress_test.py` | 并发下单压测脚本（Python 标准库，无依赖） |
| `test_api.sh` | API 冒烟测试 + 完整演示序列 |

> 配套讲解：`javaee/docs/05-JavaEE企业级开发-现代实战教程.md`