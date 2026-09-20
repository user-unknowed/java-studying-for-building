package com.example.scada.protocol;

/**
 * Modbus RTU 帧工具（串口场景用）。
 *
 * <p>RTU 帧结构（对比 TCP：没有 MBAP 头，多了 CRC 校验）：
 * <pre>
 * ┌──────┬──────┬───── 数据 ─────┬─────────┐
 * │ 从站号│ 功能码│  地址/数量/值   │ CRC16(2) │
 * └──────┴──────┴───────────────┴─────────┘
 * CRC 低字节在前 —— 这是 Modbus 世界里最著名的"坑"之一。
 * </pre>
 *
 * <p>本类只负责"构帧 + 校验"这两件事，收发交给 {@link ModbusSerialClient}。
 */
public final class ModbusRtu {

    private ModbusRtu() {
    }

    /** 构建"读保持寄存器"请求帧：从站号 + 0x03 + 起始2 + 数量2 + CRC2。 */
    public static byte[] buildReadRequest(int slaveAddr, int start, int count) {
        byte[] frame = new byte[8];
        frame[0] = (byte) slaveAddr;
        frame[1] = 0x03;
        frame[2] = (byte) (start >> 8);
        frame[3] = (byte) start;
        frame[4] = (byte) (count >> 8);
        frame[5] = (byte) count;
        appendCrc(frame, 6);
        return frame;
    }

    /** 构建"写单个寄存器"请求帧：从站号 + 0x06 + 地址2 + 值2 + CRC2。 */
    public static byte[] buildWriteRequest(int slaveAddr, int addr, int value) {
        byte[] frame = new byte[8];
        frame[0] = (byte) slaveAddr;
        frame[1] = 0x06;
        frame[2] = (byte) (addr >> 8);
        frame[3] = (byte) addr;
        frame[4] = (byte) (value >> 8);
        frame[5] = (byte) value;
        appendCrc(frame, 6);
        return frame;
    }

    /** 在帧尾追加 CRC（低字节在前）。 */
    private static void appendCrc(byte[] frame, int dataLen) {
        int crc = Crc16.of(frame, dataLen);
        frame[dataLen] = (byte) (crc & 0xFF);
        frame[dataLen + 1] = (byte) ((crc >> 8) & 0xFF);
    }

    /** 校验整帧 CRC（末尾 2 字节为帧内 CRC）。 */
    public static boolean checkCrc(byte[] frame, int len) {
        if (len < 4) {
            return false;
        }
        int calc = Crc16.of(frame, len - 2);
        int recv = (frame[len - 2] & 0xFF) | ((frame[len - 1] & 0xFF) << 8);
        return calc == recv;
    }
}