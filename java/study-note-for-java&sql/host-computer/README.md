# host-computer 分区 —— Java 上位机 / 工业数据采集

> 学习阶梯的"工业落地"段：把 JavaSE 语法与 Java×SQL 技能，用到真实的工业场景——
> 串口 / 以太网通信、Modbus 协议、数据采集与监控。

## 📖 文档

| 文档 | 内容 |
|---|---|
| [06-Java上位机开发实战：串口 · Modbus · 数据采集与监控](docs/06-Java上位机开发实战-串口·Modbus·数据采集与监控.md) | 0~9 章 + 附录：通信基础 → Modbus 手写协议帧（TCP/RTU + CRC16）→ 数据入库 → 控制与报警 → 可靠性工程 → Swing 界面 → 真机迁移（含真实 bug 复盘） |

## 💻 项目（全部真实运行验证）

| 项目 | 技术栈 | 练什么 |
|---|---|---|
| [device-monitor](demos/device-monitor/) | Java 17 · jSerialComm · SQLite JDBC + Python 模拟器 | TCP / 串口双链路、Modbus 帧与 CRC16、入库、控制闭环、报警上升沿 |

## 🚀 快速开始

```bash
cd demos/device-monitor
bash download-deps.sh        # 下载依赖（jSerialComm + SQLite JDBC）
bash scripts/sim_test.sh     # 契约测试：原始字节验证下位机模拟器
bash scripts/run_demo.sh     # TCP 全流程：采集 → 报表 → 控制 → 报警
bash scripts/run_serial.sh   # 串口（PTY）全流程：Modbus RTU + CRC16
```

## 🗺️ 前置知识（建议先完成）

- 《JavaSE基础知识点-完整梳理》第 14~16 章（现代语法 / 外部库 / 网络编程）——项目里高频使用；
- 《Java与SQL数据库-深度融合教学》——数据入库的 JDBC 六步模式直接复用；
- 《JavaEE企业级开发-现代实战教程》——SQLite 并发结论（`WAL + busy_timeout`）直接复用。

---
*本分区与 `javase/`、`javaee/` 分区共同构成完整的 Java 学习阶梯：语言地基 → 服务端开发 → 工业应用。*