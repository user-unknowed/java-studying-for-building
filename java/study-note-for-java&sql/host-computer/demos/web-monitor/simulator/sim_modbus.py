#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
下位机模拟器（Modbus TCP / Modbus RTU 双模式）
================================================
模拟一台"温室环境监测仪"从站设备，供 Java 上位机教程联调使用。

保持寄存器表（4x 区，0 基地址）：
    0  : 温度 ×10     (int16, 如 245 = 24.5℃)
    1  : 湿度 ×10
    2  : 光照        (lux)
    3  : 土壤湿度 ×10
    9  : 温度报警上限 ×10（上位机可写，功能码 0x06）
    10 : 报警标志（0/1，只读）
    11 : 累计报警次数（只读）
    12 : 设备状态字（0x0001 = 运行中）

支持功能码：0x03（读保持寄存器）、0x06（写单个寄存器）

用法：
    python3 sim_modbus.py --mode tcp --port 15020              # Modbus TCP
    python3 sim_modbus.py --mode serial --pty-file ./pty.txt   # Modbus RTU（PTY 虚拟串口）

配套教程：《Java 上位机开发实战：串口 · Modbus · 数据采集与监控》
"""
import argparse
import math
import os
import random
import socket
import struct
import threading
import time
import tty


# ---------------------------------------------------------------- CRC16
def crc16_modbus(data: bytes) -> int:
    """CRC16/MODBUS：多项式 0xA001（反射），初始值 0xFFFF。RTU 帧校验用。"""
    crc = 0xFFFF
    for b in data:
        crc ^= b
        for _ in range(8):
            crc = (crc >> 1) ^ 0xA001 if crc & 1 else crc >> 1
    return crc


# ---------------------------------------------------------------- 寄存器表
class RegisterBank:
    """带锁的保持寄存器表 + 简单的物理量仿真。"""

    def __init__(self):
        self.lock = threading.Lock()
        self.regs = [0] * 32
        self.max_temp = 280          # 默认报警上限 28.0℃（×10）
        self.alarms_total = 0
        self._temp = 24.0            # 内部真实值（浮点）
        self._humid = 55.0
        self._soil = 45.0
        self._tick = 0

    def update(self):
        """物理量演化：每 0.5s 调一次（缓慢漂移 + 噪声）。"""
        self._tick += 1
        base = 24.0 + 1.5 * math.sin(self._tick / 40.0)
        self._temp += (base - self._temp) * 0.05 + random.uniform(-0.2, 0.2)
        self._humid += random.uniform(-0.6, 0.6)
        self._soil += random.uniform(-0.4, 0.4)
        self._humid = min(80.0, max(35.0, self._humid))
        self._soil = min(65.0, max(25.0, self._soil))
        light = max(0, int(600 + 300 * math.sin(self._tick / 25.0) + random.uniform(-40, 40)))

        with self.lock:
            t10 = round(self._temp * 10)
            self.regs[0] = t10 & 0xFFFF
            self.regs[1] = round(self._humid * 10) & 0xFFFF
            self.regs[2] = light
            self.regs[3] = round(self._soil * 10) & 0xFFFF
            self.regs[9] = self.max_temp
            # 报警：温度 ≥ 上限 置位；回落至 上限-0.5℃ 以下复位
            if t10 >= self.max_temp:
                if self.regs[10] == 0:
                    self.alarms_total += 1
                self.regs[10] = 1
            elif t10 < self.max_temp - 5:
                self.regs[10] = 0
            self.regs[11] = self.alarms_total & 0xFFFF
            self.regs[12] = 0x0001

    def snapshot(self):
        with self.lock:
            return list(self.regs)

    def set(self, addr, value):
        with self.lock:
            if 0 <= addr < len(self.regs):
                self.regs[addr] = value & 0xFFFF
                if addr == 9:
                    self.max_temp = value & 0xFFFF


# ---------------------------------------------------------------- PDU 处理
def handle_pdu(bank: RegisterBank, fn: int, payload: bytes) -> bytes:
    """处理一个 PDU（功能码 + 数据），返回响应 PDU。异常时返回 (fn|0x80, code)。"""
    if fn == 0x03:   # 读保持寄存器
        start, count = struct.unpack(">HH", payload[:4])
        if not (0 <= start < 32 and 1 <= count <= 16 and start + count <= 32):
            return bytes([0x83, 0x02])          # 非法数据地址
        data = bank.snapshot()[start:start + count]
        body = b"".join(struct.pack(">H", v) for v in data)
        return bytes([0x03, len(body)]) + body
    if fn == 0x06:   # 写单个寄存器
        addr, value = struct.unpack(">HH", payload[:4])
        if not (0 <= addr < 32):
            return bytes([0x86, 0x02])
        bank.set(addr, value)
        return bytes([0x06]) + struct.pack(">HH", addr, value)
    return bytes([fn | 0x80, 0x01])             # 非法功能码


def log(*a):
    print(time.strftime("[%H:%M:%S]"), *a, flush=True)


# ---------------------------------------------------------------- TCP 模式
def recv_exact(conn, n):
    buf = b""
    while len(buf) < n:
        try:
            chunk = conn.recv(n - len(buf))
        except OSError:
            return b""
        if not chunk:
            return b""
        buf += chunk
    return buf


def serve_tcp(bank: RegisterBank, port: int):
    srv = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    srv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    srv.bind(("0.0.0.0", port))
    srv.listen(8)
    log(f"[SIM] Modbus TCP 从站已监听 :{port}")

    def client_thread(conn, addr):
        log(f"[SIM] 上位机已连接 {addr}")
        try:
            while True:
                head = recv_exact(conn, 7)
                if not head:
                    break
                tid, pid, length = struct.unpack(">HHH", head[:6])
                unit = head[6]
                pdu = recv_exact(conn, length - 1)      # length = unit(1) + PDU(n)
                if not pdu:
                    break
                fn = pdu[0]
                resp = handle_pdu(bank, fn, pdu[1:])
                log(f"[SIM] TCP fn=0x{fn:02X} 请求={pdu.hex()} 响应={resp.hex()}")
                frame = struct.pack(">HHH", tid, pid, len(resp) + 1) + bytes([unit]) + resp
                conn.sendall(frame)
        except (ConnectionError, OSError):
            pass
        finally:
            conn.close()
            log(f"[SIM] 上位机断开 {addr}")

    while True:
        conn, addr = srv.accept()
        threading.Thread(target=client_thread, args=(conn, addr), daemon=True).start()


# ---------------------------------------------------------------- 串口(PTY)模式
def serve_serial(bank: RegisterBank, pty_file: str):
    master_fd, slave_fd = os.openpty()
    slave_name = os.ttyname(slave_fd)
    tty.setraw(slave_fd)
    os.set_blocking(master_fd, False)
    if pty_file:
        with open(pty_file, "w") as f:
            f.write(slave_name)
    log(f"[SIM] Modbus RTU 从站已就绪，虚拟串口 = {slave_name}")

    buf = b""
    while True:
        time.sleep(0.01)
        try:
            data = os.read(master_fd, 4096)
        except OSError:
            data = b""
        if data:
            buf += data
        # RTU 读/写寄存器请求固定 8 字节：addr + fn + 4字节数据 + crc2
        while len(buf) >= 8:
            frame, buf = buf[:8], buf[8:]
            addr, fn = frame[0], frame[1]
            calc = crc16_modbus(frame[:6])
            recv_crc = frame[6] | (frame[7] << 8)
            if calc != recv_crc:
                log(f"[SIM] RTU CRC 错: recv={recv_crc:04X} calc={calc:04X} frame={frame.hex()}")
                continue
            resp_pdu = handle_pdu(bank, fn, frame[2:6])
            resp = bytes([addr]) + resp_pdu
            resp += struct.pack("<H", crc16_modbus(resp))
            log(f"[SIM] RTU addr={addr} fn=0x{fn:02X} 请求={frame.hex()} 响应={resp.hex()}")
            os.write(master_fd, resp)


# ---------------------------------------------------------------- main
def main():
    ap = argparse.ArgumentParser(description="Modbus 下位机模拟器（温室监测仪）")
    ap.add_argument("--mode", choices=["tcp", "serial"], default="tcp")
    ap.add_argument("--port", type=int, default=15020)
    ap.add_argument("--pty-file", default="/tmp/device_pty.txt")
    args = ap.parse_args()

    bank = RegisterBank()

    def physics_loop():
        while True:
            bank.update()
            time.sleep(0.5)

    threading.Thread(target=physics_loop, daemon=True).start()

    if args.mode == "tcp":
        serve_tcp(bank, args.port)
    else:
        serve_serial(bank, args.pty_file)


if __name__ == "__main__":
    main()
