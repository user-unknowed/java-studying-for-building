package com.example.scada.protocol;

/**
 * Modbus 通信 / 协议层异常。
 *
 * <p>设计为受检异常：上位机与设备通信是"随时可能失败"的物理过程
 * （线缆松动、设备断电、电磁干扰……），编译器强制调用方认真处理，
 * 这正是工控软件该有的严谨。
 */
public class ModbusException extends Exception {

    public ModbusException(String message) {
        super(message);
    }

    public ModbusException(String message, Throwable cause) {
        super(message, cause);
    }
}