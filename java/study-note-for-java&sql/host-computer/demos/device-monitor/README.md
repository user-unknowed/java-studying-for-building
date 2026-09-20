# device-monitor —— Java 上位机演示（温室环境监控）

一个真实可运行的**工业数据采集最小系统**：Python 下位机模拟器 + Java 上位机，Modbus TCP / 串口 RTU 双链路。

完整走通 **通信 → 协议（手写帧 / CRC16 校验）→ 入库（SQLite）→ 控制下发 → 报警 → Swing 监控界面** 全链路。

> 配套教程：《06-Java 上位机开发实战：串口 · Modbus · 数据采集与监控》（`../../docs/`）
> 所有功能均已实测；关键运行输出存档在 `run_output.txt`（含一次真实 bug 的"修复前 / 修复后"对比）。

## 它演示了什么

- **下位机**（`simulator/sim_modbus.py`，纯 Python 标准库）：行为等价于一台"温室环境监测仪"——
  温度 / 湿度 / 光照 / 土壤湿度映射为 Modbus 保持寄存器，支持 TCP 与串口（PTY）两种模式；
- **上位机**（Java 17）：手写 Modbus 协议帧（TCP + RTU/CRC16）、轮询采集、SQLite 入库、
  控制下发（写寄存器）、报警上升沿检测、控制台 + Swing 双界面。

## 目录结构

```
device-monitor/
├── download-deps.sh              # 下载依赖：jSerialComm + SQLite JDBC → lib/
├── simulator/sim_modbus.py       # 下位机模拟器（TCP / 串口 PTY 双模式）
├── src/com/example/scada/
│   ├── protocol/                 # Modbus TCP/RTU、CRC16、异常码
│   ├── link/                     # 串口抽象：jSerialComm(真机) / 文件流(PTY 调试)
│   ├── model/                    # record：DeviceReading / AlarmEvent
│   ├── db/                       # SQLite 仓库（readings / alarms / ops_log）
│   ├── service/                  # 轮询采集 + 报警上升沿
│   ├── ui/                       # ConsoleDashboard / MonitorFrame(Swing)
│   └── Main.java                 # 入口：monitor / report / control / gui
├── scripts/                      # 一键脚本 + 契约测试
└── data/  ·  out/                # 运行期生成（数据库/日志/编译产物）
```

## 快速开始

```bash
cd device-monitor

# 0) 下载依赖（首次，约 15MB）
bash download-deps.sh

# 1) 契约测试：用原始字节验证模拟器（纯 Python，不经 Java）
bash scripts/sim_test.sh

# 2) TCP 链路全流程：采集 → 报表 → 下发控制 → 报警 → 报表
bash scripts/run_demo.sh

# 3) 串口（PTY）链路全流程：Modbus RTU + CRC16
bash scripts/run_serial.sh

# 4) Swing 监控窗口（需要桌面环境）
bash scripts/run_gui.sh
```

## 技术要点

- **协议层与链路层分离**：同一份 Modbus RTU 代码，切换 `SerialTransport` 实现即可在真机串口与 PTY 调试间无缝迁移；
- **大端序与 `& 0xFF`**：协议解析的两条"肌肉记忆"（`0xEF` 在 Java 里是负数，必须 `& 0xFF` 翻回 0~255）；
- **WAL + busy_timeout**：SQLite 并发连接串参数（复用 order-service 案例结论）；
- **报警上升沿**：只在 0→1 的瞬间记录一次，避免"每轮都喊"刷屏。

## 已知限制

- 本源代码在 **Linux（无显示终端）** 下开发验证；Swing GUI 做了编译级验证，桌面环境可直接运行；
- jSerialComm 会拒绝对 PTY 虚拟设备打开（`code=13`，属正常行为）——脚本会自动改用"文件流"实现，真实 USB 串口不受影响。

## 依赖

| jar | 用途 |
|---|---|
| `jSerialComm-2.11.0.jar` | 真机串口通信 |
| `sqlite-jdbc.jar` | SQLite 历史数据存储 |
