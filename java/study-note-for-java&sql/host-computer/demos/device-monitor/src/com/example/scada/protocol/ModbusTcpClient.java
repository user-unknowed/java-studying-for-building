package com.example.scada.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Modbus TCP 主站（上位机侧）客户端 —— 手写协议帧版，零第三方依赖。
 *
 * <p>Modbus TCP 帧结构（从零看懂工业协议）：
 * <pre>
 * ┌──────────── MBAP 头（7 字节）─────────────┬───── PDU ──────┐
 * │ 事务ID(2) 协议ID(2) 长度(2) 单元号(1)     │ 功能码(1)+数据  │
 * └───────────────────────────────────────────┴────────────────┘
 * 事务ID : 上位机自增编号，用于"响应配对"（防止串包）
 * 协议ID : 恒为 0（Modbus 专属）
 * 长度   : 后续字节数 = 单元号(1) + PDU 长度
 * 单元号 : 相当于从站地址（TCP 场景常见 1）
 * </pre>
 *
 * <p>支持功能码：0x03 读保持寄存器、0x06 写单个寄存器。
 * 实现了 {@link AutoCloseable}，推荐 try-with-resources 使用。
 */
public class ModbusTcpClient implements AutoCloseable {

    private final String host;
    private final int port;
    private final int unitId;
    private final int timeoutMs;

    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private int transactionId = 1;

    public ModbusTcpClient(String host, int port) {
        this(host, port, 1, 3000);
    }

    public ModbusTcpClient(String host, int port, int unitId, int timeoutMs) {
        this.host = host;
        this.port = port;
        this.unitId = unitId;
        this.timeoutMs = timeoutMs;
    }

    /** 建立 TCP 连接（连接超时 + 读超时，避免"卡死等数据"）。 */
    public void connect() throws ModbusException {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            socket.setSoTimeout(timeoutMs);
            socket.setTcpNoDelay(true); // 工业小包场景，禁用 Nagle 攒包
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            throw new ModbusException("连接下位机失败 " + host + ":" + port + " → " + e.getMessage(), e);
        }
    }

    /**
     * 功能码 0x03：读保持寄存器。
     *
     * @param start 起始地址（0 基；4x 区 40001 对应地址 0）
     * @param count 寄存器个数（1~125）
     * @return 每个寄存器一个 16 位无符号值
     */
    public int[] readHoldingRegisters(int start, int count) throws ModbusException {
        byte[] pdu = {
                0x03,
                (byte) (start >> 8), (byte) start,
                (byte) (count >> 8), (byte) count
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

    /** 功能码 0x06：写单个寄存器（上位机下发控制参数的通道）。 */
    public void writeSingleRegister(int addr, int value) throws ModbusException {
        byte[] pdu = {
                0x06,
                (byte) (addr >> 8), (byte) addr,
                (byte) (value >> 8), (byte) value
        };
        byte[] resp = transact(pdu);
        // 写响应 PDU = fn(1) + 地址(2) + 值(2)，共 5 字节；地址在 [1..2]，值在 [3..4]
        int echoAddr = ((resp[1] & 0xFF) << 8) | (resp[2] & 0xFF);
        int echoValue = ((resp[3] & 0xFF) << 8) | (resp[4] & 0xFF);
        if (echoAddr != addr || echoValue != value) {
            throw new ModbusException("写寄存器回显不符: addr=" + echoAddr + " value=" + echoValue);
        }
    }

    /** 事务核心：组帧 → 发送 → 收响应 → 逐层校验（事务ID / 异常码）。 */
    private byte[] transact(byte[] pdu) throws ModbusException {
        ensureConnected();
        int tid = transactionId++ & 0xFFFF;
        try {
            // ① 组装完整帧 = MBAP 头 + PDU
            int length = pdu.length + 1;              // 长度字段 = 单元号 + PDU
            byte[] frame = new byte[7 + pdu.length];
            frame[0] = (byte) (tid >> 8);
            frame[1] = (byte) tid;
            frame[2] = 0;
            frame[3] = 0;                             // 协议ID 恒为 0
            frame[4] = (byte) (length >> 8);
            frame[5] = (byte) length;
            frame[6] = (byte) unitId;
            System.arraycopy(pdu, 0, frame, 7, pdu.length);

            // ② 发送
            out.write(frame);
            out.flush();

            // ③ 读 MBAP 头（7 字节）→ 按长度读 PDU
            byte[] head = readFully(7);
            int rTid = ((head[0] & 0xFF) << 8) | (head[1] & 0xFF);
            int rLength = ((head[4] & 0xFF) << 8) | (head[5] & 0xFF);
            byte[] respPdu = readFully(rLength - 1);

            // ④ 校验事务ID（请求与响应配对）
            if (rTid != tid) {
                throw new ModbusException("事务ID不匹配: 请求=" + tid + " 响应=" + rTid);
            }

            // ⑤ 校验异常响应（功能码最高位置 1 表示设备回了一个"异常码"）
            int fn = respPdu[0] & 0xFF;
            if ((fn & 0x80) != 0) {
                int code = respPdu[1] & 0xFF;
                throw new ModbusException("Modbus异常响应: fn=0x" + Integer.toHexString(fn)
                        + " code=0x" + Integer.toHexString(code) + " (" + describeException(code) + ")");
            }
            return respPdu;
        } catch (IOException e) {
            closeQuietly();
            throw new ModbusException("通信失败: " + e.getMessage(), e);
        }
    }

    private static String describeException(int code) {
        return switch (code) {
            case 0x01 -> "非法功能码";
            case 0x02 -> "非法数据地址";
            case 0x03 -> "非法数据值";
            case 0x04 -> "从站设备故障";
            case 0x05 -> "确认(处理中)";
            case 0x06 -> "从站设备忙";
            case 0x08 -> "存储奇偶性差错";
            case 0x0A -> "网关路径不可用";
            default -> "未知异常码";
        };
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

    private void ensureConnected() throws ModbusException {
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            throw new ModbusException("尚未连接（先调用 connect()）");
        }
    }

    private void closeQuietly() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public void close() {
        closeQuietly();
        socket = null;
        in = null;
        out = null;
    }
}