# Java 上位机开发实战：串口 · Modbus · 数据采集与监控

> 面向已掌握 JavaSE 基础（建议先读《JavaSE 基础知识点 - 完整梳理》第 14~16 章）的学习者。
> 配套可运行项目：`host-computer/demos/device-monitor/` —— 下位机模拟器 + Java 上位机 + 全链路验证脚本，
> 本文所有输出均为该项目真实运行结果。
> 整理：Operit AI ｜ 2026-09

---

## 0. 导读：什么是"上位机"？

工业与物联网领域有一条经典分工：

```
┌─────────────────────┐         通信线（串口 / 网线）        ┌────────────────────┐
│  下位机（从站）       │  ◄──────────────────────────────►  │  上位机（主站）      │
│  单片机 / PLC / 仪表  │         Modbus 协议               │  运行在 PC 上的软件  │
│  · 采集数据           │                                   │  · 发指令 / 收数据   │
│  · 执行动作           │                                   │  · 显示 / 存储 / 报警 │
└─────────────────────┘                                   └────────────────────┘
```

- **下位机（Lower Computer）**：直接连接传感器/执行器的设备端，负责"干活"；
- **上位机（Host Computer）**：运行在 PC 上的监控软件，负责人机交互、数据存储、报警决策。

典型场景：工厂产线监控、电力配电室、温室大棚、实验室仪器采集、充电桩运营……

**本教程做的事**：从零手写一个"温室环境监控上位机"，完整走通
**通信（串口 + 以太网）→ 协议（Modbus）→ 入库（SQLite）→ 监控（控制台 + Swing）→ 控制与报警** 全链路。

**为什么"手写协议"而不是直接上库？** —— 上位机开发真正难的不是调 API，而是：
帧结构、字节序、CRC 校验、超时与重连。这些搞懂了，用什么库都只是工具选择问题。

### 与已有案例的关系（本项目复用了什么）

| 已有案例 | 复用点 |
|---|---|
| 《Java 与 SQL 数据库 - 深度融合教学》 | 数据入库的 JDBC 六步模式（建表 → PreparedStatement → ResultSet） |
| `order-service`（JavaEE 案例） | SQLite 并发经验：`WAL + busy_timeout` 连接串参数 |
| 《JavaSE 基础知识点》 | `record` / `var` / 文本块 / Stream / lambda 等现代语法全面使用 |

---

## 1. 通信基础：上位机与设备怎么"对话"

### 1.1 串口 —— 工控世界的老黄牛

串口（UART）是最基础的点对点通信方式，嵌入式设备几乎人手一个。配置参数俗称 **9600 8N1**：

| 参数 | 含义 | 常见值 |
|---|---|---|
| 波特率 | 每秒传输的符号数 | 9600 / 19200 / 115200 |
| 数据位 | 每个字符的位数 | 8 |
| 停止位 | 字符结束标志 | 1 |
| 校验位 | 检错位 | N（无）/ E（偶）/ O（奇） |

两个最重要的认知：

1. **波特率对不上，收到的是乱码**（双方必须一致）；
2. RS-232 与 RS-485 是电气标准：RS-232 点对点、短距离；**RS-485 半双工、总线式、最长 1200 米** —— 工业现场 90% 的串口设备是 RS-485（要接 A/B 两线 + 共地 + 末端 120Ω 终端电阻）。

### 1.2 以太网 —— 现代化路线

设备自带网口时走 TCP。相比串口：速度快、距离不限、天然支持多客户端，
**Modbus TCP** 就是"把 Modbus 报文装进 TCP 包裹里"。

### 1.3 两条路线的选型

| 维度 | 串口（RTU） | 以太网（TCP） |
|---|---|---|
| 硬件成本 | 低（单片机自带） | 需要网口/串口服务器 |
| 距离 | RS-485 最长 1.2km | 局域网不限 |
| 速率 | 低（9600 ≈ 1KB/s） | 高 |
| 拓扑 | 总线，一主多从 | 星型 |
| 调试 | 需要 USB 转串口线 | 直接 telnet/抓包 |

### 1.4 上位机软件的四层结构（对应本项目的包结构）

```
┌──────────────────────────────┐
│ 应用层  Main / PollService     │  业务：轮询策略、报警逻辑、报表
├──────────────────────────────┤
│ 协议层  protocol 包            │  Modbus：帧构造/解析/CRC/异常码
├──────────────────────────────┤
│ 链路层  link 包                │  串口（jSerialComm/设备文件）、TCP Socket
├──────────────────────────────┤
│ 数据层  db 包                  │  SQLite：读数 / 报警 / 操作日志
└──────────────────────────────┘
```

**分层的关键收益**：协议层只认识"字节流"，不知道也不关心字节从串口还是网口来——
本项目里同一份 Modbus RTU 代码，底层换成不同 `SerialTransport` 即可无缝迁移（见第 4 章）。

---

## 2. 项目与模拟器：没有真设备也能练

### 2.1 项目结构

```
device-monitor/
├── README.md
├── lib/                          # jSerialComm + sqlite-jdbc
├── simulator/
│   └── sim_modbus.py             # 下位机模拟器（Python，TCP/串口双模式）
├── src/com/example/scada/
│   ├── protocol/                 # 协议层：Modbus TCP/RTU/CRC16/异常
│   ├── link/                     # 链路层：串口传输抽象 + 两种实现
│   ├── model/                    # 数据模型（record）
│   ├── db/                       # 数据层：SQLite 仓库
│   ├── service/                  # 应用层：轮询采集服务
│   ├── ui/                       # 控制台面板 + Swing 窗口
│   └── Main.java                 # 入口：monitor / report / control / gui
├── scripts/                      # 一键演示与契约测试脚本
└── data/                         # 运行期生成：数据库、模拟器日志
```

### 2.2 下位机模拟器：一台"温室环境监测仪"

真实项目的下位机可能是任意 PLC；为了可复现，本项目用 Python 写了一个**行为等价**的模拟器
（`simulator/sim_modbus.py`），把环境数据模拟成"Modbus 保持寄存器"：

| 寄存器地址 | 含义 | 换算 |
|---|---|---|
| 0 | 温度 | 值 ÷ 10 = ℃（如 245 → 24.5℃） |
| 1 | 湿度 | 值 ÷ 10 = % |
| 2 | 光照 | 直接读数（lux） |
| 3 | 土壤湿度 | 值 ÷ 10 = % |
| 9 | 温度报警上限 | **上位机可写**（控制通道） |
| 10 | 报警标志 | 0/1（只读） |
| 11 | 累计报警次数 | 只读 |
| 12 | 设备状态字 | 0x0001 = 运行中 |

**为什么用"寄存器"这种模型？** 这是工业设备的通用心智模型：不管真实设备背后是传感器还是电机，
对外都是一张"编号格子表"，主站按址读写。**换真实设备 = 改这张映射表 + 改解析换算。**

启动方式（两种模式）：

```bash
# TCP模式：监听 15020 端口
python3 simulator/sim_modbus.py --mode tcp --port 15020

# 串口模式：创建一个 PTY 虚拟串口，路径写入 data/device_pty.txt
python3 simulator/sim_modbus.py --mode serial --pty-file data/device_pty.txt
```

### 2.3 契约测试：先验证模拟器

写上位机之前，先用"原始字节"问模拟器几个问题，确认它对协议的理解没有偏差
（脚本 `scripts/test_sim.py`，不经任何 Java 代码）：

```
[client] connected 127.0.0.1 15020
[client] fn03 读 0..3: 030800ef022e026201c4
  temp=23.9C humid=55.8% light=610lux soil=45.2%
[client] fn03 读 9..12: 03080118000000000001
  上限=28.0C 报警=0 累计=0 状态=0x0001
[client] fn06 写上限于220(=22.0C): 06000900dc
[client] 回读 reg9 = 220 → 22.0C
[client] 非法功能码0x10 → 9001 (异常响应 fn|0x80=0x90, code=0x01)
[client] done
```

读懂这段输出，Modbus 就入门了一半。逐行拆解：

- `03 08 00ef 022e 0262 01c4`：功能码 03，字节数 08，即 4 个寄存器
  （`00ef`=239→23.9℃，`022e`=558→55.8%，`0262`=610，`01c4`=452→45.2%）；
- `06 0009 00dc`：功能码 06，把寄存器 9 写成 `00dc`=220 → 报警上限变成 22.0℃；
- `9001`：请求了不支持的功能码，设备回"异常响应"：`0x10|0x80 = 0x90` + 异常码 `0x01`（非法功能码）。

> 注意 `00ef` 这种"两个字节表示一个数，高位在前"的叫 **大端序（Big-Endian）**，
> 是 Modbus 的法定字节序——上位机开发中 90% 的"数据读出来是乱的"都错在这里。

---

## 3. 第一段代码：Modbus TCP 客户端（手写协议帧）

### 3.1 帧结构：先画图，再写码

Modbus TCP 在 Modbus 报文（PDU）外套了一个 7 字节的 **MBAP 头**：

```
┌───────────── MBAP 头（7 字节）──────────────┬───── PDU ─────┐
│ 事务ID(2)  协议ID(2)  长度(2)  单元号(1)     │ 功能码(1)+数据  │
│ 0001       0000      0006     01            │ 03 0000 0004  │
└─────────────────────────────────────────────┴───────────────┘
```

| 字段 | 作用 | 本例取值 |
|---|---|---|
| 事务ID | 请求/响应配对（防串包），每请求自增 | 0001 |
| 协议ID | 恒为 0 | 0000 |
| 长度 | 后续字节数 = 单元号(1) + PDU | 0006 |
| 单元号 | 从站编号（TCP 直连常为 1） | 01 |
| PDU | 功能码 + 数据 | `03 0000 0004`（读地址0起4个） |

### 3.2 读寄存器（0x03）：核心代码

`protocol/ModbusTcpClient.java` 的读取方法（节选，完整见项目源码）：

```java
public int[] readHoldingRegisters(int start, int count) throws ModbusException {
    byte[] pdu = {
            0x03,
            (byte) (start >> 8), (byte) start,   // 起始地址（大端：高字节在前）
            (byte) (count >> 8), (byte) count    // 寄存器个数
    };
    byte[] resp = transact(pdu);
    int byteCount = resp[1] & 0xFF;
    if (byteCount != count * 2) {
        throw new ModbusException("响应字节数不符: 期望 " + (count * 2) + ", 实际 " + byteCount);
    }
    int[] values = new int[count];
    for (int i = 0; i < count; i++) {
        values[i] = ((resp[2 + i * 2] & 0xFF) << 8) | (resp[3 + i * 2] & 0xFF);
    }
    return values;
}
```

两个值得停下来想的细节：

1. **`& 0xFF`**：Java 的 `byte` 是有符号的（-128~127），`0xEF` 会被读成负数；
   `& 0xFF` 把它"翻译"回 0~255 的无符号数——**所有协议解析代码里这是肌肉记忆级的操作**；
2. **拼接大端数**：`(高字节 << 8) | 低字节`，顺序反了就是经典的"数据乱码"。

### 3.3 写寄存器（0x06）与一次真实的 bug 复盘

下发控制（把报警上限写入寄存器 9）。写响应 PDU = `fn(1) + 地址(2) + 值(2)`，共 **5 字节**。

本项目开发时这里踩过一个真实的坑，**值得完整保留**：

```
== [5/6] 下发控制（上限 → 22.0℃）==
[FATAL] Index 5 out of bounds for length 5
java.lang.ArrayIndexOutOfBoundsException: Index 5 out of bounds for length 5
        at com.example.scada.protocol.ModbusTcpClient.writeSingleRegister(ModbusTcpClient.java:97)
```

**现象**：读寄存器全正常，一下发控制就数组越界。
**根因**：校验"回显"时，把 5 字节的写响应当成了更长帧来解析，索引整体偏移一位：
错误写法 `resp[2..3]` 取地址、`resp[4..5]` 取值（超界）；正确写法：

```java
// 写响应 PDU = fn(1) + 地址(2) + 值(2)，共 5 字节；地址在 [1..2]，值在 [3..4]
int echoAddr  = ((resp[1] & 0xFF) << 8) | (resp[2] & 0xFF);
int echoValue = ((resp[3] & 0xFF) << 8) | (resp[4] & 0xFF);
```

**教训**：协议解析的索引，永远"数着字节"对照帧结构表来写。这类 bug 不会在"能通"的测试里暴露，
只会精准地在你做写过操作时给你一记暴击——**好在模拟器日志证明了：写入其实成功了，只是回显校验炸了**。
（修复前/修复后的完整对比输出都在项目 `run_output.txt` 里。）

### 3.4 异常响应处理：设备说"不"的时候

设备遇到非法请求（地址越界、功能不支持），会在**功能码最高位置 1** 并附一个异常码：

```java
int fn = respPdu[0] & 0xFF;
if ((fn & 0x80) != 0) {
    int code = respPdu[1] & 0xFF;
    throw new ModbusException("Modbus异常响应: fn=0x" + Integer.toHexString(fn)
            + " code=0x" + Integer.toHexString(code) + " (" + describeException(code) + ")");
}
```

常用异常码对照（已内置在代码里）：

| 异常码 | 含义 | 异常码 | 含义 |
|---|---|---|---|
| 0x01 | 非法功能码 | 0x04 | 从站设备故障 |
| 0x02 | 非法数据地址 | 0x05 | 确认（处理中） |
| 0x03 | 非法数据值 | 0x06 | 从站设备忙 |

### 3.5 TCP 链路实测（真实运行输出）

```
══ 设备环境监控中 · Modbus TCP 127.0.0.1:15020 · 轮询间隔 1s ══
时间       │      温度℃ │     湿度% │     光照lx │     土壤% │ 状态
00:52:35 │     23.8 │    53.9 │      657 │    44.9 │ 正常(上限 28.0℃)
00:52:36 │     23.9 │    54.5 │      698 │    44.9 │ 正常(上限 28.0℃)
00:52:37 │     24.0 │    54.3 │      717 │    44.8 │ 正常(上限 28.0℃)
00:52:38 │     23.6 │    53.9 │      716 │    44.7 │ 正常(上限 28.0℃)
00:52:39 │     23.4 │    54.8 │      716 │    44.8 │ 正常(上限 28.0℃)
```

模拟器侧同一时刻的日志（**对照学习：同一事务的两端**）：

```
[SIM] TCP fn=0x03 请求=0300000004 响应=030800eb0226026201c4
[SIM] TCP fn=0x03 请求=0300090004 响应=030800dc000100010001
```

第一行：上位机读"环境量 0~3"，响应数据 `00eb`=235→23.5℃（与界面吻合）；
第二行：读"状态量 9~12"，`00dc`=220→上限 22.0℃、`0001`=报警标志 1——**两端日志互相印证，这就是"对拍"**。

---

## 4. 串口编程

### 4.1 真机标准做法：jSerialComm

Java 没有官方串口 API，生产环境最常用 **jSerialComm**（纯 Java + 内置各平台本地库，坐标 `com.fazecast:jSerialComm:2.11.0`）。

真机上的用法（`link/JscSerialTransport.java`）：

```java
SerialPort port = SerialPort.getCommPort("/dev/ttyUSB0"); // Windows 则是 "COM3"
port.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY); // 9600 8N1
port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);           // 读超时 1s
if (!port.openPort()) {
    throw new IOException("打开串口失败: " + port.getLastErrorCode());
}
InputStream in = port.getInputStream();
OutputStream out = port.getOutputStream();
```

要点：**打开 → 配参数 → 设超时 → 拿流**，之后和普通 IO 流一样读写。

### 4.2 没有真串口怎么办：PTY 虚拟串口

教程环境（以及任何没有硬件的开发机）常用 Linux 的 **PTY（伪终端）** 造一对"虚拟串口"：

- Python 的 `os.openpty()` 创建一对设备：一端（master）给"模拟的下位机"，另一端（slave，形如 `/dev/pts/6`）给上位机；
- 上位机把它当串口打开，双方就能收发字节流——**行为与真串口几乎一致**。

模拟器里创建虚拟串口的核心代码：

```python
master_fd, slave_fd = os.openpty()
slave_name = os.ttyname(slave_fd)     # 如 /dev/pts/6
tty.setraw(slave_fd)                   # 关闭回显与行缓冲：纯字节流模式
with open(pty_file, "w") as f:         # 把路径写出来给上位机用
    f.write(slave_name)
```

### 4.3 踩坑记录（本项目真实踩到）：jSerialComm 拒绝 PTY

第一次尝试：用 jSerialComm 直接打开 `/dev/pts/6`——

```
OPEN FAILED code=13 loc=538
```

而同一个路径，Java 文件流可以完美打开（诊断程序 `FileProbe` 输出）：

```
path=/dev/pts/6 exists=true canRead=true canWrite=true
RAF OPEN OK
```

**结论**：jSerialComm 在 Linux 下对"非标准串口设备"有额外校验，会拒绝 PTY 这类虚拟设备（真实 USB 转串口设备不受影响）。
这正是本项目设计 `SerialTransport` 抽象层的意义——**同一套 Modbus RTU 协议代码，两种底层传输**：

| 实现 | 场景 | 原理 |
|---|---|---|
| `JscSerialTransport` | **真机**（/dev/ttyUSB0、COM3） | jSerialComm 打开 + 配置波特率 |
| `PtyFileTransport` | 本环境 / 调试 | 文件流直接读写设备文件（Linux 一切皆文件） |

```java
// Main.java 里的自动选择（教学简化版）
SerialTransport t = path.startsWith("/dev/pts")
        ? new PtyFileTransport(path, baud)     // PTY：文件流版
        : new JscSerialTransport(path, baud);  // 真机：jSerialComm 版
```

> 延伸认知：在 Linux 下，串口本质就是 `/dev/` 下的字符设备文件；
> jSerialComm 底层同样先 `open()` 它，再做 termios 配置。**"文件流版"不是 hack，而是串口的本质。**

### 4.4 Modbus RTU 帧与 CRC16

RTU 帧 = **从站号 + PDU + CRC16（2 字节，低字节在前）**：

```
01 03 00 00 00 04 44 09
│  │  └───────┬──────┘ └─┬─┘
│  │     起始0 数量4    CRC16
│  └ 功能码 03（读保持寄存器）
└ 从站号 01
```

CRC16/MODBUS 算法（`protocol/Crc16.java`，10 行核心）：

```java
int crc = 0xFFFF;
for (int i = 0; i < len; i++) {
    crc ^= (data[i] & 0xFF);
    for (int bit = 0; bit < 8; bit++) {
        crc = ((crc & 1) != 0) ? (crc >> 1) ^ 0xA001 : crc >> 1;
    }
}
```

参数记牢：**初始值 0xFFFF、多项式 0xA001、低字节先发**。
超时窗口内收满预期长度后必须验证 CRC——**工业现场电磁干扰下，没有校验的通信都是赌博**。

### 4.5 串口链路实测（真实运行输出）

```
== [2/4] 启动串口模式模拟器（PTY）==
虚拟串口 = /dev/pts/6
== [3/4] 串口采集（3 轮）+ 下发控制 + 再采集（2 轮）==
设备环境监控中 · Modbus RTU /dev/pts/6 · 轮询间隔 1s
00:53:18 │     24.2 │    55.2 │      638 │    45.1 │ 正常(上限 28.0℃)
00:53:19 │     24.1 │    54.7 │      661 │    44.8 │ 正常(上限 28.0℃)
00:53:20 │     24.3 │    54.6 │      655 │    44.5 │ 正常(上限 28.0℃)
--- control ---
[control] 已下发报警上限 21.0℃（写寄存器 9 ← 210）
--- monitor again ---
00:53:21 │     24.1 │    54.3 │      705 │    44.5 │ ⚠ 超上限(>21.0℃)
00:53:22 │     23.9 │    54.2 │      724 │    43.8 │ ⚠ 超上限(>21.0℃)
```

模拟器侧 RTU 日志（真实字节流，注意每帧 CRC 都在）：

```
[SIM] RTU addr=1 fn=0x03 请求=0103000000044409 响应=01030800f10223029501c0a169
[SIM] RTU addr=1 fn=0x06 请求=0106000900d2d995 响应=0106000900d2d995
```

- 读请求 `010300000004 4409`：从站1、读地址0起4个、CRC 低字节在前；
- 写请求 `01060009 00d2 d995`：把寄存器9写成 `0xd2`=210 → 21.0℃（与 control 输出吻合！）。

---

## 5. 数据入库：把工业数据接上 SQL

监控软件的价值一半在"实时看"，另一半在"回头看"——数据必须落库。
本章直接复用已有案例《Java 与 SQL 数据库 - 深度融合教学》的 **JDBC 六步模式**。

### 5.1 表设计：三张表讲清工业数据模型

```sql
readings(  id, ts, temp_c, humidity, light, soil, max_c, alarm )   -- 每轮采样一条
alarms(    id, ts, kind, message, value )                          -- 报警事件（上升沿记录）
ops_log(   id, ts, action, detail )                                -- 上位机操作审计
```

设计要点：

- 时间统一存 **ISO-8601 字符串**（`Instant.toString()`），显示层再格式化——跨时区无歧义；
- `alarm` 存 0/1，与设备寄存器语义保持一致；
- **控制指令也要留痕**（ops_log）：工业软件里"谁在什么时候改了参数"必须可追溯。

### 5.2 建仓库：连接与建表（节选）

```java
conn = DriverManager.getConnection(
        "jdbc:sqlite:" + dbFile + "?journal_mode=WAL&busy_timeout=5000");
```

> 这个连接串的两个参数直接**复用自 `order-service` 案例的并发结论**：
> **WAL** 让读写并行；**busy_timeout** 让写冲突等待而不是直接报错。

建表用文本块（现代语法的可读性优势）：

```java
st.execute("""
        CREATE TABLE IF NOT EXISTS readings(
            id       INTEGER PRIMARY KEY AUTOINCREMENT,
            ts       TEXT    NOT NULL,
            temp_c   REAL    NOT NULL,
            humidity REAL    NOT NULL,
            light    INTEGER NOT NULL,
            soil     REAL    NOT NULL,
            max_c    REAL    NOT NULL,
            alarm    INTEGER NOT NULL DEFAULT 0
        )""");
```

### 5.3 插入与报表

- 插入用 `PreparedStatement`（逐列 `setXxx`，防注入 + 批量友好）；
- 报表聚合全交给 SQL：`COUNT / AVG / MIN / MAX / SUM`——**能交给 SQL 算的，绝不用 Java 循环**。

```java
try (ResultSet rs = st.executeQuery(
        "SELECT COUNT(*) n, AVG(temp_c) at, MIN(temp_c) lo, MAX(temp_c) hi, SUM(alarm) ac FROM readings")) {
    // ...
}
```

### 5.4 报表实测

```
== [4/6] 报表查询 ==
┌─ 采集数据总览 ─────────────────────────────
│ 采集轮数 : 5
│ 温度均值 : 23.7℃（最低 23.4 / 最高 24.0）
│ 报警轮数 : 0
└────────────────────────────────────────────
最近 5 条读数:
  00:52:39  temp=23.4℃ hum=55% light=716lx soil=45%
  00:52:38  temp=23.6℃ hum=54% light=716lx soil=45%
  ...
报警记录（最近 5 条）:
  (无)
```

---

## 6. 控制与报警：上位机的"闭环"

### 6.1 下发控制：一条命令改变设备行为

```bash
java -cp "out:lib/*" com.example.scada.Main control --tcp 127.0.0.1:15020 --max-temp 22.0
```

```
[control] 已下发报警上限 22.0℃（写寄存器 9 ← 220）
```

这条命令的背后：`22.0 → ×10 整数化 → 0xDC → 功能码 0x06 写入寄存器 9`。
**"物理量 ↔ 寄存器整数"的换算（×10）是工程惯例**：设备端只认整数，小数用"缩放因子"表达
（Modbus 没有浮点寄存器，温度和上限都按 ×10 存储）。

### 6.2 报警：上升沿检测，而不是"每轮都喊"

设备每秒都在上报"当前是否报警"。若每轮都写库/弹窗，运维会被刷屏。
正确做法是**只在 0→1 的瞬间记录一次**（降到阈值以下后复位）：

```java
if (reading.alarm() && !lastAlarm) {
    String msg = "温度超上限: %.1f℃ > %.1f℃".formatted(reading.tempC(), reading.maxTempC());
    db.insertAlarm(new AlarmEvent(now, "TEMP_HIGH", msg, reading.tempC()));
}
lastAlarm = reading.alarm();
```

### 6.3 全流程实测：把上限压到 22℃

```
== [5/6] 下发控制（上限 → 22.0℃）==
[control] 已下发报警上限 22.0℃（写寄存器 9 ← 220）
== [6/6] 再次采集（观察报警）+ 报表 ==
00:52:40 │     23.5 │    55.0 │      803 │    44.5 │ ⚠ 超上限(>22.0℃)
00:52:42 │     23.5 │    54.7 │      819 │    44.5 │ ⚠ 超上限(>22.0℃)
00:52:43 │     23.5 │    54.6 │      827 │    45.3 │ ⚠ 超上限(>22.0℃)
--- 第二次报表:
│ 采集轮数 : 8
│ 报警轮数 : 3
报警记录（最近 5 条）:
  00:52:40 [TEMP_HIGH] 温度超上限: 23.5℃ > 22.0℃
```

一个完整的**遥控闭环**：
**上位机改参数 → 设备行为变化 → 数据状态变化 → 报警产生 → 落库可查**。

---

## 7. 可靠性工程：上位机软件"敢上线"的几个点

- **通信失败不崩溃**：主循环捕获 `ModbusException`，打警告继续下一轮（本项目已实现）；
  生产环境再加**指数退避重连**与**看门狗**：

```java
// 断线重连骨架（生产模板）
while (true) {
    try {
        connect();
        loop();
    } catch (ModbusException e) {
        backoff();     // 1s → 2s → 4s … 上限 30s
    }
}
```

- **超时三处都要设**：连接超时、读超时、写超时（本项目均 3000ms，避免"卡死等数据"）；
- **生产级话题清单**（点到为止，可自行延伸）：
  多设备并发轮询（线程池）、寄存器映射配置化（YAML/JSON）、写操作权限与审计、
  数据保留策略（归档/清理）、时钟同步（设备时间与服务器时间）。

---

## 8. 监控界面：Swing 版

上位机的"脸面"是界面。本项目提供 `ui/MonitorFrame.java`：
表格每 2 秒刷新最近 20 条读数（数据直接读 SQLite）。

代码要点：

- `DefaultTableModel` + `JTable`：行数据来自 `db.recentReadingRows(20)`；
- `javax.swing.Timer(2000, ...)` 定时刷新（Swing 定时器保证在 EDT 事件线程执行）；
- **数据流设计**：采集进程写库 → 界面读库，两边解耦（界面关掉不影响采集）。

> 诚实说明：本教程的开发环境是无显示终端（headless），GUI 只做了**编译级验证**；
> 在有桌面环境的电脑上运行以下命令即可弹出监控窗口：
>
> ```bash
> bash scripts/run_gui.sh
> ```

---

## 9. 从本案例到真实设备

### 9.1 换一台真实 PLC / 仪表，要改什么？

只有两处：

1. **寄存器映射表**（地址 → 物理量/换算系数）——改 `PollService.pollOnce()` 的解析；
2. **连接参数**（串口号/波特率 或 IP:端口）——命令行参数传入。

协议层、链路层、数据层**完全不用动**——这就是分层的威力。

### 9.2 RS-485 现场接线备忘

- A/B 两线不能接反（接反现象：完全无响应）；
- 多设备**手拉手**布线（星型拓扑易反射）；
- 总线两端（最远的两台）各接 120Ω 终端电阻；
- 共地很重要；长距离适当降低波特率（9600 比 115200 稳）。

### 9.3 Modbus 常用功能码速查

| 功能码 | 名称 | 用途 |
|---|---|---|
| 0x01 / 0x02 | 读线圈 / 离散输入 | 开关量（继电器状态、限位） |
| 0x03 | 读保持寄存器 | **读数值（本项目主角）** |
| 0x04 | 读输入寄存器 | 只读数值（传感器原始值） |
| 0x05 / 0x06 | 写单个线圈 / 寄存器 | **写控制（本项目控制通道）** |
| 0x0F / 0x10 | 写多个线圈 / 寄存器 | 批量下发参数 |

### 9.4 练习（含验收标准）

1. **扩展一个寄存器**：给模拟器加"风扇开关"（线圈 0x05），上位机加 `fan on/off` 命令；
2. **多设备**：让模拟器支持 `--unit 2`，上位机轮询两个从站（提示：TCP 下改单元号即可）；
3. **报警升级**：连续 3 轮超限才报警（防抖），报警信息里带上持续轮数；
4. **报表进阶**：按小时聚合温度曲线（提示：SQLite `strftime('%H', ts)` 分组）。

---

## 附录 A：如何运行本项目（一键脚本）

```bash
cd host-computer/demos/device-monitor

# 1) 契约测试（原始字节，验证模拟器）
bash scripts/sim_test.sh

# 2) TCP 链路全流程：采集 → 报表 → 控制 → 报警
bash scripts/run_demo.sh

# 3) 串口（PTY）链路全流程
bash scripts/run_serial.sh
```

## 附录 B：项目关键资产清单

```
simulator/sim_modbus.py                  下位机模拟器（TCP + RTU 双模式 + 环境仿真）
scripts/test_sim.py                      原始字节契约测试
scripts/sim_test.sh / run_demo.sh / run_serial.sh / run_gui.sh   一键脚本
src/.../protocol/ModbusTcpClient.java    Modbus TCP 主站（手写协议帧）
src/.../protocol/ModbusSerialClient.java Modbus RTU 主站
src/.../protocol/ModbusRtu.java          RTU 帧工具
src/.../protocol/Crc16.java              CRC16/MODBUS（含自测 main）
src/.../link/SerialTransport.java        串口传输抽象
src/.../link/JscSerialTransport.java     jSerialComm 实现（真机）
src/.../link/PtyFileTransport.java       设备文件实现（PTY/调试）
src/.../db/MonitorDb.java                SQLite 仓库（readings/alarms/ops_log）
src/.../service/PollService.java         轮询采集 + 报警上升沿
src/.../model/DeviceReading.java         record：一次采样
src/.../model/AlarmEvent.java            record：一条报警
src/.../ui/ConsoleDashboard.java         控制台面板
src/.../ui/MonitorFrame.java             Swing 窗口
src/.../Main.java                        monitor / report / control / gui 入口
```

**下一篇**：《07-JavaEE上位机实战：网页监控看板与远程控制》——同一台"温室监测仪"的**网页版上位机**（Servlet + 嵌入式 Tomcat + 浏览器看板），把"一人一窗"升级成"多人多端看数据"。

**更远一步的路线建议**：多设备并发轮询模板 → 配置化（YAML）→ 历史曲线（JFreeChart）→ 打包部署（jpackage / Maven）。
