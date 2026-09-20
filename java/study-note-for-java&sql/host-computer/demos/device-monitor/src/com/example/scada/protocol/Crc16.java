package com.example.scada.protocol;

/**
 * CRC16/MODBUS 校验算法（串口 RTU 帧的"防伪标记"）。
 *
 * <p>原理一句话：把整帧字节按位"滚动异或"，得到一个 16 位指纹；
 * 接收方算出的指纹与帧尾附带的指纹一致，才认为传输没有出错。
 *
 * <p>参数（Modbus 规范规定，背下来）：
 * <ul>
 *   <li>初始值 0xFFFF</li>
 *   <li>多项式 0xA001（是标准 CRC-16 多项式 0x8005 的"位反转"形式）</li>
 *   <li>发送时低字节在前（小端）</li>
 * </ul>
 *
 * <p>为什么不用 Java 现成库？——因为算法就这 10 行，自己写得清清楚楚，
 * 而且任意资料里对得上号（工业现场调试必备技能）。
 */
public final class Crc16 {

    private Crc16() {
    }

    /** 计算 data[0..len) 的 CRC16/MODBUS 值。 */
    public static int of(byte[] data, int len) {
        int crc = 0xFFFF;
        for (int i = 0; i < len; i++) {
            crc ^= (data[i] & 0xFF);
            for (int bit = 0; bit < 8; bit++) {
                if ((crc & 1) != 0) {
                    crc = (crc >> 1) ^ 0xA001;
                } else {
                    crc >>= 1;
                }
            }
        }
        return crc & 0xFFFF;
    }

    public static int of(byte[] data) {
        return of(data, data.length);
    }

    /** 自测：算一个已知帧的 CRC，并与模拟器（Python 侧同算法）对拍。 */
    public static void main(String[] args) {
        byte[] frame = {(byte) 0x01, 0x03, 0x00, 0x00, 0x00, 0x04}; // 读 0~3 寄存器
        int crc = of(frame);
        System.out.printf("帧 %s 的 CRC16 = 0x%04X%n", toHex(frame), crc);
        System.out.printf("实际附在帧尾（低字节在前）: %02X %02X%n", crc & 0xFF, (crc >> 8) & 0xFF);
    }

    private static String toHex(byte[] bytes) {
        var sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}