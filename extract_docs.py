#!/usr/bin/env python3
"""从持久化的 MCP JSON 响应中提取干净的 markdown 文本，保存为 .md 文件"""
import json, os, re

SRC_DIR = "/data/user/work/.trae/toolcall-output"
OUT_DIR = "/workspace/docs-src"
os.makedirs(OUT_DIR, exist_ok=True)

# 映射：持久化文件 ID → 输出文件名
FILES = {
    "aaf087f5-1cba-4df8-a0cc-1aa26182da58": "01-JavaSE基础知识点-完整梳理.md",
    "1c116d7c-5c0a-4f2e-ac98-4103dfce646e": "02-Java与SQL数据库-深度融合教学.md",
    "6b90692b-db69-4c90-b709-3a7ab2dcbe25": "03-SQL数据库深度讲解-工业实践版.md",
    "e8c56dce-c936-49c8-842c-66787e30063c": "05-JavaEE企业级开发-现代实战教程.md",
    "12fb3386-e6f3-4c9b-99d8-4e4e2ab4acd2": "06-Java上位机开发实战-串口Modbus数据采集与监控.md",
    "6f37dfd8-fa2a-4a21-a955-275b79858f2b": "07-JavaEE上位机实战-网页监控看板与远程控制.md",
}

def extract_text(raw):
    """从 MCP 响应 JSON 中提取 markdown 文本"""
    # 去掉前导 "The MCP server responded with: " 前缀
    idx = raw.find("[")
    if idx == -1:
        return raw
    arr = json.loads(raw[idx:])
    for item in arr:
        if item.get("type") == "resource":
            return item["resource"]["text"]
        if item.get("type") == "text" and "text" in item:
            t = item["text"]
            if t.startswith("successfully downloaded"):
                continue
            return t
    return raw

for fid, name in FILES.items():
    src = os.path.join(SRC_DIR, f"{fid}.txt")
    if not os.path.exists(src):
        print(f"MISS: {src}")
        continue
    with open(src, "r", encoding="utf-8") as f:
        raw = f.read()
    md = extract_text(raw)
    out = os.path.join(OUT_DIR, name)
    with open(out, "w", encoding="utf-8") as f:
        f.write(md)
    print(f"OK: {name} ({len(md)} chars)")

# 04 号文档内容直接写入（之前已内联读取）
doc04 = """# 计算机学习报告：Java · 数据库 · AI Agent

> 整理日期：2026-09-20 ｜ 整理者：Operit AI
> 目的：三大方向的知识地图 + 最新动态 + 学习路径，帮助你系统化学习。

---

## 一、总览

| 方向 | 核心内容 | 2025-2026 动态 |
|---|---|---|
| Java | 语法 → OOP → 并发 → JVM → Spring 生态 | **JDK 25 LTS**（2025.9 发布）：虚拟线程、结构化并发、ScopedValue 等 |
| 数据库 | SQL → 索引/事务 → 架构优化 → NoSQL | **PostgreSQL 18**、**MySQL 9.x**（原生向量存储）、**Redis 8.0** |
| AI Agent | LLM → 工具调用 → RAG → 多智能体 | **MCP 协议**成事实标准、LangGraph/CrewAI 成熟、**OpenClaw** 爆火 |

---

## 二、Java 编程

### 2.1 学习路线（4 阶段）

1. **入门**：语法、面向对象、集合（List/Map/Set）、异常、IO
2. **进阶**：并发编程（线程池/JUC/JMM）、JVM（内存模型/GC/类加载）、网络编程
3. **工程**：Maven/Gradle、JUnit、Spring Boot、MyBatis/JPA、Redis/消息队列
4. **现代 Java**：JDK 21/25 新特性（虚拟线程、记录类、模式匹配）

### 2.2 JDK 25 LTS 亮点（2025年9月）

- **正式落地**：
  - 紧凑源文件 + 实例 main 方法（JEP 512）：无需 class 声明即可写程序
  - 灵活构造函数体（JEP 513）：`super()` 不再必须是第一句
  - 作用域值 ScopedValue（JEP 506）：虚拟线程友好的 ThreadLocal 替代
  - 紧凑对象头（JEP 519）：内存占用更小
  - 分代 Shenandoah GC（JEP 521）、密钥派生函数 API（JEP 510）
- **预览/孵化中**：模式匹配原始类型（507）、模块导入声明（511）、结构化并发第五预览（505）、稳定值 API（502）、向量 API（508）
- 32 位 x86 支持已被正式移除（JEP 503）

```java
// Java 25 紧凑源文件示例（hello.java，直接运行 java hello.java）
void main() {
    System.out.println("Hello, Java 25!");
}
```

### 2.3 核心必会清单

- 集合：HashMap 底层（数组+链表+红黑树、扩容）、ArrayList 扩容机制
- 并发：synchronized vs Lock、CAS、AQS、线程池 7 大参数与调优
- **虚拟线程**（JDK 21+）：IO 密集型高并发利器；注意 synchronized 引起的 pinning 问题
- JVM：内存分区、G1/ZGC、双亲委派、常用调优参数（-Xmx/-Xms/GC 日志）

### 2.4 推荐资源

- 书：《Java 核心技术》《Effective Java》《深入理解 Java 虚拟机》
- 在线：Baeldung 中文站、Oracle 官方 JEP 列表、LeetCode（日刷 1-2 题）
- 视频：黑马 / 尚硅谷 JDK 21/25 新特性专题

---

## 三、数据库

### 3.1 学习路线

1. **SQL 基础**：增删改查、JOIN、聚合、子查询
2. **核心原理**：索引（B+ 树）、事务 ACID、MVCC、隔离级别、锁
3. **实战优化**：EXPLAIN 执行计划、慢查询分析、分库分表
4. **架构**：主从复制、读写分离、高可用
5. **NoSQL 与趋势**：Redis、MongoDB、向量数据库

### 3.2 版本动态（2025-2026）

- **PostgreSQL 18**：异步 I/O（io_uring）大幅提升吞吐、内置 uuidv7()、虚拟生成列成默认、OAuth 2.0 认证
- **MySQL 9.x**：原生向量类型（VECTOR）支持 AI 场景、JavaScript 存储程序；8.4 为当前 LTS 线
- **Redis 8.0**：新增 Vector Set 数据结构，向 AI/检索场景靠拢

### 3.3 核心必会清单

- 索引：最左前缀、覆盖索引、回表、索引下推
- 事务：四种隔离级别 + MVCC 原理（RC 与 RR 的实现差异）
- 优化：EXPLAIN 关键字段（type / key / rows / Extra）、慢查询日志思路
- 选型：OLTP（MySQL/PG）· OLAP（ClickHouse/Doris）· KV（Redis）· 向量（Milvus/Qdrant/pgvector）

```sql
-- 经典练习：查询每个部门薪资最高的员工（窗口函数）
SELECT dept, name, salary FROM (
    SELECT dept, name, salary,
           ROW_NUMBER() OVER (PARTITION BY dept ORDER BY salary DESC) AS rn
    FROM employees
) t
WHERE rn = 1;
```

### 3.4 推荐资源

- 书：《高性能 MySQL》《数据库系统概念》
- 动手：Docker 起 MySQL/PostgreSQL，LeetCode 数据库题库日练

---

## 四、AI Agent

### 4.1 核心概念

- **公式**：Agent = LLM + 工具 + 记忆 + 规划循环
- **ReAct 模式**：推理（Reason）与行动（Act）交替，边想边做
- **Function Calling**：让 LLM 能"做事"的关键机制
- **RAG**：向量检索 + 生成，解决知识时效与幻觉问题
- **记忆分层**：短期（上下文窗口）vs 长期（向量库/数据库）

```python
# Agent 核心循环（伪代码）
while not done:
    thought = llm.think(goal, history)      # 思考
    action  = llm.choose_tool(thought)      # 选工具
    result  = tools.run(action)             # 执行
    history.append(thought, action, result) # 记忆
```

### 4.2 框架生态（2026 视角）

| 框架 | 定位 | 适合谁 |
|---|---|---|
| LangGraph | 图结构、有状态 Agent，生产级 | 复杂流程 / 平台开发者 |
| CrewAI | 角色团队式多智能体协作 | 流程自动化 / 内容管线 |
| AutoGen | 多智能体对话（微软） | 快速原型 / 研究 |
| **Spring AI / LangChain4j** | **Java 生态** Agent 开发 | **Java 开发者（与你的技能栈衔接）** |
| OpenClaw | 自托管 Gateway，把聊天 App 接入 Agent（2026 爆火） | 个人助手 / 自部署用户 |
| Dify / Coze / n8n | 低代码平台 | 快速搭建 / 非开发者 |

### 4.3 关键协议与趋势

- **MCP（Model Context Protocol）**：Anthropic 发起的"AI 工具标准接口"，已被广泛采用
- **A2A**：Agent 间协作协议（Google 发起）
- 趋势：Computer Use（操作电脑）、Agentic Coding（AI 写代码）、多智能体协作、评估与可观测性

### 4.4 推荐资源

- 文档：LangGraph 官方文档、MCP 规范、OpenAI Cookbook、Anthropic 工程博客
- 动手路径：给 LLM 一个工具 → 用 **LangChain4j** 写第一个 Java Agent → 接入 MCP 服务器

---

## 五、12 周学习计划（可调整）

| 周次 | Java | 数据库 | AI Agent |
|---|---|---|---|
| 1-4 | 语法 + OOP + 集合 | SQL 基础 + 查询练习 | LLM 与 Prompt 基础 |
| 5-8 | 并发 + JVM 基础 | 索引 + 事务原理 | 工具调用 + 第一个 Agent |
| 9-12 | Spring Boot 项目实战 | 优化 + Redis + 新版特性 | RAG + 多智能体 + MCP |

**每日节奏**：1 小时理论 + 1 小时动手 + 15 分钟复盘笔记

---

## 六、我可以怎么帮你

1. **讲解**：任何知识点随时提问，我深入浅出讲解 + 举例
2. **出题 / 测验**：按你的进度出题（选择 / 代码 / 场景题）并批改
3. **写示例代码**：Java / SQL 示例即写即讲，可在终端运行验证
4. **代码审查**：把你的代码发我，我来 review
5. **项目陪练**：从零带你做一个 Spring Boot + 数据库的小项目

> 本报告基于 2026-09 公开资料检索整理，仅供学习参考。
"""
with open(os.path.join(OUT_DIR, "04-学习报告-Java-数据库-AI-Agent.md"), "w", encoding="utf-8") as f:
    f.write(doc04)
print(f"OK: 04-学习报告 ({len(doc04)} chars)")

print("\n=== Done. Files in', OUT_DIR, ': ===")
for fn in sorted(os.listdir(OUT_DIR)):
    sz = os.path.getsize(os.path.join(OUT_DIR, fn))
    print(f"  {fn}: {sz} bytes")
