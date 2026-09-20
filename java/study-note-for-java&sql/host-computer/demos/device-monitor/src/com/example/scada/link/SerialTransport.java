package com.example.scada.link;

import java.io.Closeable;
import java.io.IOException;

/**
 * 串口传输抽象层：屏蔽底层差异，让上层协议（Modbus RTU）不关心
 * "数据究竟怎么进出的"。
 *
 * <p>两个实现：
 * <ul>
 *   <li>{@link JscSerialTransport} —— 基于 jSerialComm，真机（USB 转串口/板载串口）标准做法；</li>
 *   <li>{@link PtyFileTransport} —— 基于设备文件，本教程环境演示 / 调试兜底。</li>
 * </ul>
 *
 * <p>这正是一个"依赖倒置"的小案例：协议层依赖抽象（本接口），
 * 而不是依赖某个具体库。
 */
public interface SerialTransport extends Closeable {

    /** 打开设备。 */
    void open() throws IOException;

    /** 写入字节流（内部保证 flush）。 */
    void write(byte[] data) throws IOException;

    /**
     * 读取最多 buf.length 字节，最多等待 timeoutMs 毫秒。
     *
     * @return 实际读到的字节数；0 表示超时（无数据）。
     */
    int read(byte[] buf, int timeoutMs) throws IOException;

    /** 人类可读的描述（日志用）。 */
    String describe();
}