package com.example.webmonitor.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Modbus TCP 客户端（教学精简版）—— Web 版上位机的"数据入口"。
 *
 * <p>与 06 号教程（device-monitor）中的完整版同源，这里只保留 Web 场景用到的两个功能码：
 * 0x03 读保持寄存器 / 0x06 写单个寄存器。帧结构：
 *
 * <pre>
 * ┌──────────── MBAP 头（7 字节）────────────┬──── PDU ────┐
 * │ 事务ID(2) 协议ID(2) 长度(2) 单元号(1)     │ 功能码+数据  │
 * └───────────────────────────────────────────┴─────────────┘
 * </pre>
 *
 * <p>线程安全：本类自身不做同步，由调用方（Poller）用同一把锁串行化读/写。
 */
public class ModbusTcpClient implements AutoCloseable {

    private final String host;
    private final int port;
    private final int unitId = 1;
    private final int timeoutMs = 3000;

    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private int transactionId = 1;

    public ModbusTcpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /** 建立 TCP 连接（连接超时 + 读超时，避免"卡死等数据"）。 */
    public void connect() throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), timeoutMs);
        socket.setSoTimeout(timeoutMs);
        socket.setTcpNoDelay(true); // 工业小包场景，禁用 Nagle 攒包
        in = socket.getInputStream();
        out = socket.getOutputStream();
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    /**
     * 功能码 0x03：读保持寄存器。
     *
     * @param start 起始地址（0 基），count 个数（1~125）
     */
    public int[] readHoldingRegisters(int start, int count) throws IOException {
        byte[] pdu = {
                0x03,
                (byte) (start >> 8), (byte) start,
                (byte) (count >> 8), (byte) count
        };
        byte[] resp = transact(pdu);
        int byteCount = resp[1] & 0xFF;
        if (byteCount != count * 2) {
            throw new IOException("响应字节数不符: 期望 " + (count * 2) + ", 实际 " + byteCount);
        }
        int[] values = new int[count];
        for (int i = 0; i < count; i++) {
            values[i] = ((resp[2 + i * 2] & 0xFF) << 8) | (resp[3 + i * 2] & 0xFF);
        }
        return values;
    }

    /** 功能码 0x06：写单个寄存器（下发控制参数的通道）。 */
    public void writeSingleRegister(int addr, int value) throws IOException {
        byte[] pdu = {
                0x06,
                (byte) (addr >> 8), (byte) addr,
                (byte) (value >> 8), (byte) value
        };
        byte[] resp = transact(pdu);
        int echoAddr = ((resp[1] & 0xFF) << 8) | (resp[2] & 0xFF);
        int echoValue = ((resp[3] & 0xFF) << 8) | (resp[4] & 0xFF);
        if (echoAddr != addr || echoValue != value) {
            throw new IOException("写寄存器回显不符: addr=" + echoAddr + " value=" + echoValue);
        }
    }

    /** 事务核心：组帧 → 发送 → 收响应 → 校验（事务ID / 异常码）。 */
    private byte[] transact(byte[] pdu) throws IOException {
        int tid = transactionId++ & 0xFFFF;
        int length = pdu.length + 1;                 // 长度字段 = 单元号 + PDU
        byte[] frame = new byte[7 + pdu.length];
        frame[0] = (byte) (tid >> 8);
        frame[1] = (byte) tid;
        frame[2] = 0;
        frame[3] = 0;                                // 协议ID 恒为 0
        frame[4] = (byte) (length >> 8);
        frame[5] = (byte) length;
        frame[6] = (byte) unitId;
        System.arraycopy(pdu, 0, frame, 7, pdu.length);

        out.write(frame);
        out.flush();

        byte[] head = readFully(7);                  // MBAP 头
        int rTid = ((head[0] & 0xFF) << 8) | (head[1] & 0xFF);
        int rLength = ((head[4] & 0xFF) << 8) | (head[5] & 0xFF);
        byte[] respPdu = readFully(rLength - 1);

        if (rTid != tid) {
            throw new IOException("事务ID不匹配: 请求=" + tid + " 响应=" + rTid);
        }
        int fn = respPdu[0] & 0xFF;
        if ((fn & 0x80) != 0) {
            throw new IOException("Modbus异常响应: fn=0x" + Integer.toHexString(fn)
                    + " code=0x" + Integer.toHexString(respPdu[1] & 0xFF));
        }
        return respPdu;
    }

    private byte[] readFully(int n) throws IOException {
        byte[] buf = new byte[n];
        int off = 0;
        while (off < n) {
            int r = in.read(buf, off, n - off);
            if (r < 0) {
                throw new IOException("连接被对端关闭");
            }
            off += r;
        }
        return buf;
    }

    @Override
    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
        socket = null;
        in = null;
        out = null;
    }
}