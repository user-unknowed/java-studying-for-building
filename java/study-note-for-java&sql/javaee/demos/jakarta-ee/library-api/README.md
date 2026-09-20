# library-api —— 图书借阅管理 REST API

一个真实可运行的 **Jakarta EE Web 应用**：嵌入式 Tomcat 10.1 + Servlet 6.0 + JDBC + SQLite。

演示从"裸 Servlet"到"REST API"的最小完整闭环：

```
HTTP 请求 → Servlet（Web 层）→ DAO（数据层）→ SQLite（存储层）
```

## API 一览

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/books` | 图书列表 |
| GET | `/api/books/{id}` | 图书详情 |
| GET | `/api/books/popular` | 借阅排行榜（LEFT JOIN + GROUP BY） |
| POST | `/api/books` | 新增图书 `{"title":"...","author":"...","stock":3}` |
| POST | `/api/books/{id}/borrow` | 借书 `{"borrower":"张三"}`（事务：扣库存+写记录） |
| POST | `/api/books/{id}/return` | 还书 `{"borrower":"张三"}`（事务：标归还+库存+1） |

## 快速开始

```bash
# 1. 下载依赖 jar（Tomcat + Gson + SQLite JDBC）
bash download-deps.sh

# 2. 编译
javac -encoding UTF-8 -cp "lib/*" -d out src/com/demo/library/*.java

# 3. 启动服务（端口 8081）
java -Dfile.encoding=UTF-8 -cp "out:lib/*" com.demo.library.Main

# 4. 另开终端：跑完整测试序列（并自动存档输出）
bash test_api.sh | tee run_output.txt
```

## 代码结构

| 文件 | 职责 |
|---|---|
| `Main.java` | 嵌入式 Tomcat 启动 + Servlet 注册 |
| `BookApiServlet.java` | REST 路由（按 HTTP 方法 + 路径分发） |
| `BookDao.java` | 数据访问层（全部 SQL + 事务） |
| `Db.java` | 连接管理与建表 |
| `Book.java` | 模型 |

## 设计要点

- **借书用条件更新防超借**：`UPDATE books SET stock = stock - 1 WHERE id = ? AND stock > 0`，扣不动即无库存
- **借书/还书都是事务**：库存与借阅记录必须"同生共死"
- **分层清晰**：Servlet 只管 HTTP 语义，所有 SQL 收敛在 DAO
- **状态码语义**：201 创建成功 / 404 不存在 / 409 业务冲突（库存不足）

> 配套讲解：`javaee/docs/05-JavaEE企业级开发-现代实战教程.md`