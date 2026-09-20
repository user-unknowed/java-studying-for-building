package com.example.scada.link;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 基于 jSerialComm 的串口传输（跨平台，PC 真机标准做法）。
 *
 * <p>Maven 坐标：<code>com.fazecast:jSerialComm:2.11.0</code>
 *
 * <p>用法（真机）：
 * <pre>
 * new JscSerialTransport("/dev/ttyUSB0", 9600)   // Linux（CH340/CP2102 等 USB 转串口）
 * new JscSerialTransport("COM3", 9600)           // Windows
 * </pre>
 *
 * <p>实测提示：部分虚拟设备（如 Linux 的 PTY <code>/dev/pts/N</code>）会被
 * jSerialComm 拒绝（openPort 返回 false，code=13）。此类调试场景请改用
 * {@link PtyFileTransport}；真实的 USB 转串口设备不受影响。
 */
public class JscSerialTransport implements SerialTransport {

    private final String portPath;
    private final int baudRate;
    private SerialPort port;
    private InputStream in;
    private OutputStream out;

    public JscSerialTransport(String portPath, int baudRate) {
        this.portPath = portPath;
        this.baudRate = baudRate;
    }

    @Override
    public void open() throws IOException {
        port = SerialPort.getCommPort(portPath);
        port.setComPortParameters(baudRate, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);
        if (!port.openPort()) {
            throw new IOException("打开串口失败: " + portPath
                    + " (code=" + port.getLastErrorCode() + ")");
        }
        in = port.getInputStream();
        out = port.getOutputStream();
    }

    @Override
    public void write(byte[] data) throws IOException {
        out.write(data);
        out.flush();
    }

    @Override
    public int read(byte[] buf, int timeoutMs) throws IOException {
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, timeoutMs, 0);
        int n = in.read(buf);
        return Math.max(n, 0);
    }

    @Override
    public void close() {
        if (port != null) {
            port.closePort();
        }
        port = null;
    }

    @Override
    public String describe() {
        return "串口 " + portPath + " @ " + baudRate + "bps";
    }
}