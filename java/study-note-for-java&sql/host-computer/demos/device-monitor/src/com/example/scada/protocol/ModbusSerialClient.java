package com.example.scada.protocol;

import com.example.scada.link.SerialTransport;

import java.io.IOException;
import java.util.Arrays;

/**
 * Modbus RTU 主站（串口上位机侧）客户端。
 *
 * <p>一次"读寄存器"的完整往返：
 * <pre>
 * 上位机: 发 8 字节请求帧（01 03 00 00 00 04 CRC）
 * 下位机: 回 (1+1+1+2N+2) 字节响应帧（01 03 08 数据... CRC）
 * 上位机: ① 收满预期长度 → ② CRC 校验 → ③ 解析为寄存器数组
 * </pre>
 *
 * <p>依赖 {@link SerialTransport} 抽象 —— 同一份协议代码，
 * 换一个 transport 就能跑在 jSerialComm 或 PTY 设备文件之上。
 */
public class ModbusSerialClient implements AutoCloseable {

    private final SerialTransport transport;
    private final int slaveAddr;
    private final int readTimeoutMs;

    public ModbusSerialClient(SerialTransport transport, int slaveAddr, int readTimeoutMs) {
        this.transport = transport;
        this.slaveAddr = slaveAddr;
        this.readTimeoutMs = readTimeoutMs;
    }

    /** 功能码 0x03：读保持寄存器。响应长度 = 5 + 2*count。 */
    public int[] readHoldingRegisters(int start, int count) throws ModbusException {
        byte[] resp = exchange(ModbusRtu.buildReadRequest(slaveAddr, start, count), 5 + count * 2);
        int byteCount = resp[2] & 0xFF;
        if (byteCount != count * 2) {
            throw new ModbusException("RTU 响应字节数异常: " + byteCount);
        }
        int[] values = new int[count];
        for (int i = 0; i < count; i++) {
            values[i] = ((resp[3 + i * 2] & 0xFF) << 8) | (resp[4 + i * 2] & 0xFF);
        }
        return values;
    }

    /** 功能码 0x06：写单个寄存器（回显校验）。响应恒为 8 字节。 */
    public void writeSingleRegister(int addr, int value) throws ModbusException {
        byte[] resp = exchange(ModbusRtu.buildWriteRequest(slaveAddr, addr, value), 8);
        int eAddr = ((resp[2] & 0xFF) << 8) | (resp[3] & 0xFF);
        int eVal = ((resp[4] & 0xFF) << 8) | (resp[5] & 0xFF);
        if (eAddr != addr || eVal != value) {
            throw new ModbusException("RTU 写寄存器回显不符: addr=" + eAddr + " value=" + eVal);
        }
    }

    /** 收发核心：写请求 → 在超时窗口内收满预期长度 → CRC 校验。 */
    private byte[] exchange(byte[] request, int expectedLen) throws ModbusException {
        try {
            transport.write(request);
            byte[] buf = new byte[64];
            int got = 0;
            long deadline = System.currentTimeMillis() + readTimeoutMs;
            while (got < expectedLen && System.currentTimeMillis() < deadline) {
                byte[] tmp = new byte[64];
                int remainMs = (int) Math.max(1, deadline - System.currentTimeMillis());
                int n = transport.read(tmp, remainMs);
                if (n <= 0) {
                    break;
                }
                System.arraycopy(tmp, 0, buf, got, n);
                got += n;
            }
            if (got < expectedLen) {
                throw new ModbusException("RTU 响应超时/不完整: " + got + "/" + expectedLen + " 字节");
            }
            if (!ModbusRtu.checkCrc(buf, got)) {
                throw new ModbusException("RTU CRC 校验失败（帧可能被干扰损坏）");
            }
            if ((buf[1] & 0x80) != 0) {
                throw new ModbusException("Modbus 异常响应: code=" + (buf[2] & 0xFF));
            }
            return Arrays.copyOf(buf, got);
        } catch (IOException e) {
            throw new ModbusException("串口通信失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        try {
            transport.close();
        } catch (IOException ignored) {
        }
    }
}