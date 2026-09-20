package com.example.scada.link;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 基于"设备文件"的串口传输 —— Linux"一切皆文件"的活教材。
 *
 * <p>在 Linux 里，串口本质就是 <code>/dev/</code> 下的一个字符设备文件：
 * jSerialComm 底层也是先 <code>open()</code> 它、再做 termios 配置。
 * 本类直接把设备文件当普通流来读写，因此可以操作：
 * <ul>
 *   <li><b>PTY 虚拟串口</b>（<code>/dev/pts/N</code>）—— 本教程环境用于真机演示；</li>
 *   <li>任何无需修改波特率参数的场景。</li>
 * </ul>
 *
 * <p>注意：本类<b>无法设置波特率 / 校验位 / 停止位</b>（对 PTY 而言这些本来也无意义）。
 * 连接真实硬件时请使用 {@link JscSerialTransport}。
 */
public class PtyFileTransport implements SerialTransport {

    private final String devicePath;
    private final int baudHint;
    private FileInputStream in;
    private FileOutputStream out;

    public PtyFileTransport(String devicePath) {
        this(devicePath, 9600);
    }

    public PtyFileTransport(String devicePath, int baudHint) {
        this.devicePath = devicePath;
        this.baudHint = baudHint;
    }

    @Override
    public void open() throws IOException {
        // 串口的读写是"两扇门"：即使同一个设备文件，也需要分别打开读端口与写端口
        in = new FileInputStream(devicePath);
        out = new FileOutputStream(devicePath);
    }

    @Override
    public void write(byte[] data) throws IOException {
        out.write(data);
        out.flush();
    }

    @Override
    public int read(byte[] buf, int timeoutMs) throws IOException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            int avail = in.available();
            if (avail > 0) {
                return in.read(buf, 0, Math.min(avail, buf.length));
            }
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("读取被中断", e);
            }
        }
        return 0; // 超时
    }

    @Override
    public void close() throws IOException {
        try {
            if (in != null) {
                in.close();
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    @Override
    public String describe() {
        return "设备文件 " + devicePath + "（波特率参数 " + baudHint + " 对 PTY 无意义，演示用）";
    }
}