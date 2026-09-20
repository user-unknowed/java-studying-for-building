# JavaEE 上位机实战：网页监控看板与远程控制

> 面向已读完《06-Java 上位机开发实战》或《05-JavaEE 企业级开发》的学习者 ｜ 整理：Operit AI ｜ 2026-09
>
> 配套可运行项目：`demos/web-monitor`（真实编译、运行、验证，完整证据见 `run_output.txt`）
> 下位机沿用 06 号教程的"温室环境监测仪"模拟器 —— 同一台设备，两种上位机。

---

## 0. 导读：为什么上位机还要有"网页版"？

06 号教程做了一台**桌面版上位机**（Swing + 串口/Modbus）：值班工程师坐在工位上，看着监控窗口采集、入库、报警。它很可靠，但有两个天花板：

1. **只有装了客户端的人能看到** —— 车间主任想瞄一眼数据？得再装一套程序；
2. **一人一窗** —— 大屏看板、手机巡检、远程访问，桌面程序都不擅长。

**网页版上位机**正好补上这两块：一台服务器 / 一个采集进程跑在机房，**浏览器就是客户端**——
车间大屏打开一个固定网址、主任用手机扫个二维码、你在家打开笔记本，看到的是同一份实时数据。

两条路线的对照：

| 维度 | 桌面版（06 · device-monitor） | 网页版（本教程 · web-monitor） |
|---|---|---|
| 技术栈 | JavaSE + Swing + jSerialComm | JavaEE + Servlet + 嵌入式 Tomcat |
| 客户端 | 每台电脑装程序 | 浏览器，零安装 |
| 通信链路 | 串口 RTU / 网口 TCP 都行 | 适合网口（Modbus TCP），经网关也能接串口 |
| 多人访问 | 困难 | 天生支持 |
| 典型场景 | 工位单机、直接连设备 | 机房集中采集、多端查看、远程巡检 |

> **选型口诀：直接摸设备用桌面版，多端看数据用网页版。**

### 与已有案例的复用关系

- 下位机模拟器、寄存器契约：**直接复用 06 号教程**的 `sim_modbus.py`（同一台设备）；
- 嵌入式 Tomcat 的启动骨架：**对照 05 号教程**的 `library-api`（同一种"一个 Main 起一个 Web 服务"的写法）；
- 数据库六步法 / WAL / busy_timeout：复用 02、05 号教程的结论；
- 协议帧（0x03 / 0x06）：本项目的 `ModbusTcpClient` 是 06 版的精简孪生兄弟。

---

## 1. 架构总览：数据怎么从设备走到浏览器

```
┌─────────────┐   Modbus TCP    ┌───────────────────────────────┐
│  下位机      │◀──────────────▶│  web-monitor（一台 Java 进程）  │
│ 模拟器/PLC   │   :15020        │                               │
│ (温室监测仪) │                 │  ┌─────────┐    ┌──────────┐  │
└─────────────┘                 │  │ Poller  │───▶│ MonitorDb│  │
                                │  │ 采集线程 │    │ (SQLite) │  │
                                │  └────┬────┘    └──────────┘  │
                                │       │ 实时快照                 │
                                │  ┌────▼────────────────────┐  │
                                │  │ Servlet（嵌入式 Tomcat）  │  │
                                │  │  /api/*      /dashboard  │  │
                                │  └────┬────────────────────┘  │
                                └───────┼───────────────────────┘
                                        │ HTTP
                                ┌───────▼────────┐
                                │  浏览器（任意台） │
                                │ 看板：实时+历史+控制│
                                └────────────────┘
```

一条数据要经过 **4 层**：协议层（Modbus）→ 服务层（Poller）→ 存储层（SQLite）→ Web 层（Servlet/浏览器）。
每一层都能单独理解、单独测试——这正是分层架构的意义。

---

## 2. 工程结构与启动骨架

```
web-monitor/
├── src/com/example/webmonitor/
│   ├── Main.java            # 入口：装配一切 + 启动 Tomcat
│   ├── protocol/            # 协议层：手写 Modbus TCP 帧
│   ├── service/             # 服务层：Poller 采集线程
│   ├── db/                  # 存储层：SQLite 三张表
│   └── web/                 # Web 层：Servlet + Filter
├── web/dashboard.html       # 看板页（零依赖前端）
├── simulator/sim_modbus.py  # 下位机模拟器（复用 06 号）
└── scripts/                 # 一键启动 / 演示 / 自检
```

### 2.1 一个 Main 起一个 Web 服务

启动骨架与 `library-api` 同构，但多了三样东西：**采集线程、过滤器、看板页**（`Main.java` 节选）：

```java
// ① 历史数据库（SQLite）
var db = new MonitorDb(dbFile);

// ② 采集线程（后台，循环轮询下位机）
var poller = new Poller(simHost, simPort, intervalSec * 1000L, db);
poller.start();

// ③ Web 层：嵌入式 Tomcat（Jakarta EE 10 / Servlet 6.0）
var tomcat = new Tomcat();
tomcat.setPort(port);
tomcat.getConnector();

Context ctx = tomcat.addContext("", null);

Tomcat.addServlet(ctx, "api", new ApiServlet(poller, db));
ctx.addServletMappingDecoded("/api/*", "api");

Tomcat.addServlet(ctx, "dashboard", new DashboardServlet(webDir));
ctx.addServletMappingDecoded("/", "dashboard");
ctx.addServletMappingDecoded("/index.html", "dashboard");
```

### 2.2 Filter：给每个请求"过安检"

请求日志过滤器（`LogFilter.java`）——把"打印一行日志"这种横切逻辑从业务代码里拿出去：

```java
public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
    var http = (HttpServletRequest) request;
    long t0 = System.currentTimeMillis();
    try {
        chain.doFilter(request, response);   // 放行
    } finally {
        System.out.printf("[web] %s %s (%d ms)%n",
                http.getMethod(), http.getRequestURI(), System.currentTimeMillis() - t0);
    }
}
```

注册（`Main.java`）：

```java
var filterDef = new FilterDef();
filterDef.setFilterName("log");
filterDef.setFilter(new LogFilter());
ctx.addFilterDef(filterDef);

var filterMap = new FilterMap();
filterMap.setFilterName("log");
filterMap.addURLPattern("/*");
ctx.addFilterMap(filterMap);
```

运行后，服务端会为每个请求打印一行，实测节选：

```
[web] GET /api/summary (14 ms)
[web] GET / (8 ms)
[web] POST /api/command (19 ms)
```

> 想看 Filter 的真实效果？跑起来后多刷新几次看板，日志会一行行滚出来。

### 2.3 端口规划

| 端口 | 归属 | 说明 |
|---|---|---|
| 8090 | web-monitor（Web 服务） | 浏览器访问 |
| 15020 | 模拟器（Modbus TCP） | 与 06 号教程共用；两个教程的采集进程**不要同时开着抢同一台模拟器** |

---

## 3. 采集服务：轮询、快照与"韧性"

### 3.1 Poller 一个线程干三件事

```java
private void pollOnce() throws Exception {
    int[] env;  int[] meta;
    synchronized (ioLock) {
        ensureConnected();
        env  = client.readHoldingRegisters(0, 4);   // 温度/湿度/光照/土壤
        meta = client.readHoldingRegisters(9, 4);   // 上限/报警/累计/状态
    }

    var r = new Reading(Instant.now(), env[0] / 10.0, env[1] / 10.0, env[2], env[3] / 10.0,
            meta[0] / 10.0, meta[1] != 0);
    db.insertReading(r);                            // ① 入库（历史）
    if (r.alarm() && !lastAlarm) {                  // ② 报警上升沿
        db.insertAlarm("TEMP_HIGH", "温度超上限: ...", r.temp());
    }
    lastAlarm = r.alarm();
    latest = new Snapshot(...);                     // ③ 刷新实时快照
}
```

三个设计决定，各有原因：

1. **快照（Snapshot）+ volatile**：API 请求永远读内存里的最新值，不查库——刷新 2 秒一次也不心疼；
2. **上升沿报警**：`0→1` 的瞬间才记一条，`lastAlarm` 记住穿越前的状态（与 06 号一致）；
3. **ioLock 串行化**：浏览器点"下发"时，HTTP 线程要写 Modbus 连接；
   而采集线程可能正在读——**同一根连接必须排队**，否则帧会串。

### 3.2 断线自愈：工控软件的"韧性"

采集失败时**不退出**，而是：标记离线 → 关连接 → 下轮自动重连。我们没有只写在文档里，做了专项测试（杀模拟器 → 离线 → 重启 → 恢复）：

```
T1 初始（应在线）:
{"online":true,...,"polls":4,...}

T2 模拟器被杀（应离线 + 错误信息）:
{"online":false,"lastError":"Connection refused",...,"polls":4,...}

T3 模拟器重启（应自动恢复）:
{"online":true,...,"polls":8,...}
```

注意 `polls: 4 → 8` —— 恢复后采集**接着往上涨**，历史数据也没有断档。
看板页对离线有明确的视觉反馈：状态胶囊变成红色"离线（自动重试中）"。

---

## 4. Web 层：四个 REST 端点

`ApiServlet` 用 `switch` 做最小路由（一个 Servlet 顶四个端点）：

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/summary` | 实时快照 + 累计统计 |
| GET | `/api/readings?limit=N` | 最近 N 条读数（时间正序，图表用） |
| GET | `/api/alarms` | 最近报警记录 |
| POST | `/api/command` | 下发控制 `{"cmd":"set_temp_limit","value":21.5}` |

```java
Object payload = switch (path) {
    case "/summary"  -> summary();
    case "/readings" -> readings(intParam(req, "limit", 120));
    case "/alarms"   -> db.recentAlarms(20);
    default -> null;
};
```

### 4.1 输出 JSON 的一个小讲究

Gson 默认会把 `>` 转义成 `\u003e`（HTML 安全）。对 API 场景没必要，一行配置让它"说人话"：

```java
private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
```

### 4.2 控制下发的完整链路（一次真实的"远程操作"）

浏览器点"下发 21.5℃" → `POST /api/command` → Poller 写寄存器 9 → 模拟器收到 0x06 帧：

```
{"ok":true,"message":"已下发报警上限 21.5℃（写寄存器 9）"}
```

模拟器侧的真实报文（寄存器 9 = 0x00D7 = 215 = 21.5℃）：

```
[05:01:48] [SIM] TCP fn=0x06 请求=06000900d7 响应=06000900d7
```

**下发立刻生效**：上限降到 21.5℃ 后，温度（24℃ 上下）越限，下一轮采集即触发报警：

```
{"online":true,...,"limit":21.5,"alarm":true,"deviceAlarms":1,"totalAlarms":1}
[{"time":"05:01:49","kind":"TEMP_HIGH","message":"温度超上限: 24.5℃ > 21.5℃"}]
```

从"网页上的一个按钮"到"设备寄存器的真实变化"再到"报警记录"，全链路闭环——这就是上位机的核心业务。

---

## 5. 网页看板：从"能看"到"好看"

看板页 `web/dashboard.html` 是纯 HTML/CSS/JS——**零 CDN、零框架、离线可用**（工厂内网常常没有外网）。

### 5.1 三条设计原则

1. **信息层级**：先 KPI 卡片（温度/湿度/光照/土壤 × 大字号），再趋势图，最后明细（报警/控制）；
2. **颜色语义**：正常=青/绿，告警=红（温度卡片超限时整卡发光描边），离线=灰红状态胶囊；
3. **零依赖**：不引外部字体、图表库——所有资源都在这一个文件里，拷到哪里都能用。

### 5.2 手写 30 行 SVG 折线图

不用图表库，四步画出趋势图（`dashboard.html` 节选，实际约 60 行）：

```js
const X = i => pad.l + (w - pad.l - pad.r) * (n === 1 ? 0 : i / (n - 1));
const Y = v => h - pad.b - (v - lo) * (h - pad.t - pad.b) / (hi - lo);

const pts = rows.map((r, i) => X(i).toFixed(1) + "," + Y(m.pick(r)).toFixed(1)).join(" ");
parts.push('<polyline points="' + pts + '" fill="none" stroke="' + m.color + '" .../>');
```

要点：**自动缩放**（取序列 min/max 各留 12% 边距）、**渐变面积**（SVG linearGradient 填充到折线下方）、
**报警点标红**（`r.alarm` 时画一个小红点）、**最后一点高亮**（圆点 + 当前值）。
四个指标用顶部的"温度/湿度/光照/土壤"标签切换——对应数据里的四个字段。

### 5.3 轮询与离线兜底

```js
setInterval(refreshSummary, 2000);   // 实时卡片
setInterval(refreshChart,   4000);   // 趋势图
setInterval(refreshAlarms,  8000);   // 报警列表
```

任何一次请求失败都不会让页面"白屏"：卡片显示 `—`，状态胶囊转红，下一轮自动重试。

### 5.4 "审美"清单（给你的作品做对照）

- [ ] 有统一的配色变量（`:root { --bg: ...; --accent: ... }`），不是散落的十六进制；
- [ ] 数字用等宽数字（`font-variant-numeric: tabular-nums`），刷新时不抖动；
- [ ] 正常/告警/离线三态都有明确的颜色与文案，不靠猜；
- [ ] 响应式：`grid-template-columns: repeat(auto-fit, minmax(200px, 1fr))` —— 手机上也好看；
- [ ] 所有交互有反馈：按钮"下发中…→ ✅ / ❌"，绝不静默。

---

## 6. 运行与验证（真实输出）

```bash
cd web-monitor
bash download-deps.sh        # 首次下载依赖
bash scripts/run_web.sh      # 一键启动 → 浏览器打开 http://localhost:8090/
```

无浏览器环境用自动演示脚本：

```bash
bash scripts/run_demo.sh     # 编译 → 起服务 → API 自检 → 控制下发 → 收尾
```

实测关键输出（完整版见 `run_output.txt`）：

```
GET / → HTTP 200
<title>温室环境监控 · Web 看板</title>

[1] /api/summary
{"online": true, "temp": 24.1, "humidity": 54.9, "light": 680, "soil": 45.8,
 "limit": 28.0, "alarm": false, "deviceAlarms": 0, "polls": 4, ...}

{"ok":true,"message":"已下发报警上限 21.5℃（写寄存器 9）"}
→ 触发报警：[{"kind":"TEMP_HIGH","message":"温度超上限: 24.5℃ > 21.5℃"}]
```

---

## 7. 与桌面版对照 & 下一步

| 问题 | 桌面版（06） | 网页版（本教程） |
|---|---|---|
| 数据存哪 | 本机 SQLite | 服务器 SQLite（可换 PostgreSQL） |
| 谁在采集 | 客户端进程 | 服务端采集线程 |
| 掉线表现 | 窗口里红字告警 | 页面状态胶囊 + API `online:false` |
| 多人同时看 | 每人一套 | 天然共享同一份数据 |

**上线三件事（按顺序做）**：

1. **反向代理 + HTTPS**：Nginx 顶在前面（`proxy_pass http://127.0.0.1:8090`），顺手把 443 证书配上；
2. **登录鉴权**：在 `LogFilter` 的位置加一个"校验 Cookie/Token"的 Filter——课程作业级实现是"登录成功发一个签名 Token"；
3. **多设备扩展**：把 `Poller` 的 host/port 做成"设备表"循环轮询，`readings` 加一列 `device_id`——一张表升级成多设备平台。

**练习（含验收标准）**：

1. 给 `/api/summary` 增加"过去 1 分钟温度均值"字段（验收：curl 可见、数值随数据变化）；
2. 把报警上限范围限制从 5~50℃ 改成 10~40℃，前端同步提示（验收：越界请求返回 400）；
3. 在 `LogFilter` 里统计并打印"最近 60 秒请求数"（验收：日志可见且随刷新变化）。

---

## 附录 A：常见问题（FAQ）

**Q1：8090 端口被占用？**
`--port 8091` 换端口，或 `kill` 掉旧的 Java 进程（`ps` / `jps` 找 PID）。

**Q2：看板显示"离线（自动重试中）"？**
先看模拟器日志（`data/web_sim.log`）有没有 "从站已监听"；再看服务日志里 `lastError`
（最常见是 `Connection refused`——模拟器没起）。

**Q3：页面打开是空白？**
先 `curl -s http://localhost:8090/api/summary` 排除服务问题；再检查浏览器控制台。
本页零依赖，没有网络请求失败点；JS 语法可先用 `node --check` 自查。

**Q4：请求日志在哪看？**
服务端 stdout（`data/server.log`），`[web] ...` 开头的行就是 LogFilter 输出的。

## 附录 B：关键资产清单

| 资产 | 位置 |
|---|---|
| 上位机服务源码 | `demos/web-monitor/src/` |
| 看板页 | `demos/web-monitor/web/dashboard.html` |
| 下位机模拟器 | `demos/web-monitor/simulator/sim_modbus.py` |
| 真实运行证据 | `demos/web-monitor/run_output.txt` |
| 一键脚本 | `demos/web-monitor/scripts/` |

---

*上一篇：《06-Java 上位机开发实战：串口 · Modbus · 数据采集与监控》（桌面版）*
*至此，"桌面 + 网页"两种上位机形态、采集/存储/展示/控制/报警的完整闭环都已跑通——*
*把你车间的第一台设备接进来吧。*