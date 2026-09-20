#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""契约测试：直接以原始字节发 Modbus TCP 请求，验证模拟器应答格式。"""
import socket
import struct

HOST, PORT = "127.0.0.1", 15020


def recv_frame(s):
    head = b""
    while len(head) < 7:
        chunk = s.recv(7 - len(head))
        if not chunk:
            raise ConnectionError("connection closed")
        head += chunk
    tid, pid, length = struct.unpack(">HHH", head[:6])
    unit = head[6]
    rest = b""
    while len(rest) < length - 1:
        rest += s.recv(length - 1 - len(rest))
    return tid, unit, rest


def request(s, tid, fn, payload):
    pdu = bytes([fn]) + payload
    frame = struct.pack(">HHH", tid, 0, len(pdu) + 1) + b"\x01" + pdu
    s.sendall(frame)
    return recv_frame(s)


def main():
    s = socket.create_connection((HOST, PORT), timeout=5)
    print("[client] connected", HOST, PORT)

    _, _, resp = request(s, 1, 3, struct.pack(">HH", 0, 4))
    vals = struct.unpack(">" + "H" * ((len(resp) - 2) // 2), resp[2:])
    print("[client] fn03 读 0..3:", resp.hex())
    print("  temp=%.1fC humid=%.1f%% light=%dlux soil=%.1f%%"
          % (vals[0] / 10, vals[1] / 10, vals[2], vals[3] / 10))

    _, _, resp = request(s, 2, 3, struct.pack(">HH", 9, 4))
    vals = struct.unpack(">HHHH", resp[2:])
    print("[client] fn03 读 9..12:", resp.hex())
    print("  上限=%.1fC 报警=%d 累计=%d 状态=0x%04X"
          % (vals[0] / 10, vals[1], vals[2], vals[3]))

    _, _, resp = request(s, 3, 6, struct.pack(">HH", 9, 220))
    print("[client] fn06 写上限于220(=22.0C):", resp.hex())

    _, _, resp = request(s, 4, 3, struct.pack(">HH", 9, 1))
    (v,) = struct.unpack(">H", resp[2:])
    print("[client] 回读 reg9 =", v, "→ %.1fC" % (v / 10))

    _, _, resp = request(s, 5, 0x10, struct.pack(">HH", 0, 1))
    print("[client] 非法功能码0x10 →", resp.hex(), "(异常响应 fn|0x80=0x90, code=0x01)")

    s.close()
    print("[client] done")


if __name__ == "__main__":
    main()