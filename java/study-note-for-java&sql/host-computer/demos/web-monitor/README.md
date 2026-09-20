# web-monitor —— Web 版上位机（温室环境监控看板）

JavaEE 技术栈的"上位机"：**后台轮询采集（Modbus TCP）+ SQLite 历史库 + Servlet API + 网页看板**。
浏览器打开即用，无需安装客户端；多人大屏、手机、远程访问共用同一个页面。

> 配套教程：《07-JavaEE 上位机实战：网页监控看板与远程控制》（`../../docs/`）
> 与桌面版（06 教程 device-monitor）共用同一台"下位机模拟器"和同一套寄存器契约。

## 它演示了什么

- **采集线程**（Poller）：后台轮询下位机（0~3 环境量 + 9~12 状态量），断线自动重连；
- **Web 层**（Servlet + 嵌入式 Tomcat 10.1）：4 个 REST 端点 + 看板页 + 请求日志 Filter；
- **网页看板**：深色主题、SVG 折线图、2 秒自动刷新、控制下发 —— 零 CDN、零前端框架；
- **历史库**（SQLite）：readings / alarms / ops_log 三张表，WAL + busy_timeout。

关键运行输出存档在 `run_output.txt`（含"控制下发触发报警"与"断线自愈"的完整真实记录）。

## 目录结构

```
web-monitor/
├── download-deps.sh              # 下载依赖：Tomcat + Gson + SQLite JDBC + 注解 API → lib/
├── src/com/example/webmonitor/
│   ├── Main.java                 # 入口：装配部件 + 启动嵌入式 Tomcat
│   ├── model/Reading.java        # record：一次设备读数
│   ├── protocol/ModbusTcpClient.java  # 手写 Modbus TCP 帧（0x03 / 0x06）
│   ├── db/MonitorDb.java         # SQLite 历史库（三张表）
│   ├── service/Poller.java       # 采集线程 + 实时快照 + 报警上升沿 + 断线自愈
│   └── web/
│       ├── ApiServlet.java       # /api/*：summary / readings / alarms / command
│       ├── DashboardServlet.java # 看板页托管
│       └── LogFilter.java        # 请求日志 Filter（教学示例）
├── web/dashboard.html            # 看板页（纯 HTML/CSS/JS，零依赖）
├── simulator/sim_modbus.py       # 下位机模拟器（与 06 教程同一台"温室监测仪"）
├── scripts/
│   ├── run_web.sh                # 一键启动：模拟器（后台）+ 服务（前台）
│   ├── run_demo.sh               # 全自动演示：编译 → 起服务 → API 自检 → 控制下发 → 收尾
│   └── test_api.sh               # 对运行中服务的 API 自检
└── data/ · out/ · tomcat.*/      # 运行期生成（已 gitignore）
```

## 快速开始

```bash
cd web-monitor

# 0) 下载依赖（首次，约 17MB）
bash download-deps.sh

# 1) 一键启动（模拟器 + Web 服务，前台运行）
bash scripts/run_web.sh
# 浏览器打开 http://localhost:8090/  → 看板

# 2) 或全自动演示（无需浏览器，输出全套真实证据）
bash scripts/run_demo.sh
```

## API 一览

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/summary` | 实时快照 + 累计统计（在线状态 / 温度 / 湿度 / 光照 / 土壤 / 上限 / 报警） |
| GET | `/api/readings?limit=N` | 最近 N 条读数（折线图数据源，时间正序） |
| GET | `/api/alarms` | 最近报警记录 |
| POST | `/api/command` | 下发控制：`{"cmd":"set_temp_limit","value":21.5}` |

## 技术要点

- **嵌入式 Tomcat 编程式注册**：`Tomcat.addServlet` / `addServletMappingDecoded` / `FilterDef + FilterMap`（对照 javaee 分区 library-api）；
- **并发安全**：采集线程与 HTTP 控制线程共用同一把锁（ioLock）串行化 Modbus 连接上的读写；
- **断线自愈**：采集失败 → 快照标记离线（页面显示"离线（自动重试中）"）→ 下轮自动重连，实测 `polls: 4 → 8` 恢复；
- **报警上升沿**：只在 0→1 的瞬间记一条报警，避免每轮刷屏；
- **前端三条原则**：信息层级（先 KPI 后明细）、颜色语义（正常/告警）、零依赖（离线可用）。

## 依赖

| jar | 用途 |
|---|---|
| `tomcat-embed-core-10.1.60.jar` | 嵌入式 Tomcat（Jakarta EE 10 / Servlet 6.0） |
| `gson-2.11.0.jar` | JSON 序列化 |
| `sqlite-jdbc-3.47.0.0.jar` | SQLite 历史存储 |
| `jakarta.annotation-api-2.1.1.jar` | Tomcat 加载 Servlet 注解所需 |

## 已知说明

- 本仓库开发环境为无桌面 Linux：看板页通过 curl 校验（HTTP 200 + 标题），
  前端脚本通过 `node --check` 语法校验；浏览器体验请在本机运行后打开；
- 接真实 PLC / 网关：把 `--sim-host / --sim-port` 指向现场设备即可（特殊点表按需调整寄存器映射）。
