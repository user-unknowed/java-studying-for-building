# shortlink-service —— 短链接服务（Spring Boot 版）

> Java EE 教学项目 ②·现代工程篇 ｜ 场景：把长网址压缩成「短码」，访问短链时 **302 跳转** 到原址并统计点击。

## 业务场景

微博、短信、二维码里经常要把冗长的 URL 变短。短链服务是最经典的「小而完整」的后端项目：

- 写：生成短码（可自定义）→ 入库（短码唯一）
- 读：`GET /{code}` → 查库 → 302 跳转 + 点击计数

麻雀虽小，五脏俱全：**HTTP 状态码语义、唯一约束、并发计数、参数校验**全都涉及。

## API 一览

| 方法 | 路径 | 说明 | 成功码 |
|---|---|---|---|
| POST | `/api/links` | 创建短链（可带 `customCode`） | 201 |
| GET | `/api/links` | 短链列表（最近 20 条） | 200 |
| GET | `/api/links/{code}` | 短码统计（点击数等） | 200 |
| GET | `/{code}` | **核心**：302 跳转到原始 URL | 302 |

错误响应统一为 `{"error": "...", "type": "..."}`；业务冲突 409、不存在 404、参数错误 400。

## 运行方式

```bash
# 1. 打包（首次会下载依赖）
mvn -q -DskipTests package

# 2. 启动（8083 端口）
java -jar target/shortlink-service-1.0.0.jar

# 3. 另一个终端运行接口测试
bash test_api.sh | tee run_output.txt
```

## 设计要点（为什么这么写）

1. **302 而不是 301**：301 会被浏览器长期缓存，后续访问不再回源，点击统计就失真了——短链服务必须用 302。
2. **短码生成**：Base62（62 个字符）随机 7 位，62^7 ≈ 3.5 万亿组合；仍以 `UNIQUE` 约束 + 重试兜底。
3. **并发计数**：`UPDATE link SET clicks = clicks + 1`，单条 SQL 原子完成；避免「读-改-写」的丢失更新。
4. **分层纪律**：Controller 只收发、Service 管业务、Repository 管 SQL——与原生 Jakarta EE 版一脉相承。

## 与原生版对照

本项目与 `demos/jakarta-ee/order-service`（手写 Servlet + 手动事务）互为对照：
同样的分层思想，在 Spring Boot 里由框架自动完成「组件装配、事务代理、JSON 序列化」。

---
*配套教程：`docs/05-JavaEE企业级开发-现代实战教程.md`（第 9 章有完整迁移对照）*