package com.example.webmonitor.service;

import com.example.webmonitor.db.MonitorDb;
import com.example.webmonitor.model.Reading;
import com.example.webmonitor.protocol.ModbusTcpClient;

import java.time.Instant;

/**
 * 采集服务：一个后台线程，循环做三件事——
 * ① 读设备（0~3 环境量 + 9~12 状态量）；② 判断报警上升沿并入库；③ 刷新"实时快照"。
 *
 * <p>断线不退出：失败 → 快照标记离线 → 关闭连接 → 下轮自动重连（工控软件要有的"韧性"）。
 *
 * <p>并发安全：连接上的读 / 写都用同一把锁（ioLock）串行化 ——
 * HTTP 线程可能在任意时刻下发控制命令，不能和采集线程的读帧"撞车"。
 */
public class Poller {

    /** 实时快照（API 层直接读它，避免每次请求都查库）。 */
    public record Snapshot(
            boolean online, String lastError, String lastTime,
            double temp, double humidity, int light, double soil,
            double limit, boolean alarm, int deviceAlarms, boolean deviceRunning,
            int pollCount) {

        public static Snapshot offline(String error, int polls) {
            return new Snapshot(false, error, "--", 0, 0, 0, 0, 0,
                    false, 0, false, polls);
        }
    }

    private final String host;
    private final int port;
    private final long intervalMs;
    private final MonitorDb db;

    private final Object ioLock = new Object();
    private ModbusTcpClient client;
    private boolean lastAlarm = false;
    private int pollCount = 0;
    private volatile Snapshot latest = Snapshot.offline("启动中…", 0);
    private volatile boolean running = true;

    public Poller(String host, int port, long intervalMs, MonitorDb db) {
        this.host = host;
        this.port = port;
        this.intervalMs = intervalMs;
        this.db = db;
    }

    /** 启动后台采集线程（daemon，随主进程退出）。 */
    public void start() {
        var t = new Thread(this::loop, "poller");
        t.setDaemon(true);
        t.start();
    }

    private void loop() {
        while (running) {
            try {
                pollOnce();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                closeQuietly();
                latest = Snapshot.offline(shortMsg(e), pollCount);
            }
            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /** 采集一轮：读寄存器 → 解析 → 入库 → 上升沿报警 → 刷新快照。 */
    private void pollOnce() throws Exception {
        int[] env;
        int[] meta;
        synchronized (ioLock) {
            ensureConnected();
            env = client.readHoldingRegisters(0, 4);   // 温度/湿度/光照/土壤
            meta = client.readHoldingRegisters(9, 4);  // 上限/报警/累计/状态(0x0001=运行)
        }

        var now = Instant.now();
        boolean alarm = meta[1] != 0;
        var r = new Reading(now, env[0] / 10.0, env[1] / 10.0, env[2], env[3] / 10.0,
                meta[0] / 10.0, alarm);

        db.insertReading(r);

        // 报警"上升沿"：只在 0→1 的瞬间记一条，避免每轮刷屏
        if (alarm && !lastAlarm) {
            db.insertAlarm("TEMP_HIGH",
                    "温度超上限: %.1f℃ > %.1f℃".formatted(r.temp(), r.limit()), r.temp());
        }
        lastAlarm = alarm;
        pollCount++;

        latest = new Snapshot(true, null, r.timeStr(),
                r.temp(), r.humidity(), r.light(), r.soil(), r.limit(),
                alarm, meta[2], meta[3] == 1, pollCount);
    }

    /** HTTP 线程下发控制：写寄存器 9（报警上限 ×10）。与采集线程用同一把锁串行。 */
    public void setTempLimit(double value) throws Exception {
        int raw = (int) Math.round(value * 10);
        synchronized (ioLock) {
            ensureConnected();
            client.writeSingleRegister(9, raw);
        }
        db.logOp("SET_TEMP_LIMIT", "value=%.1f".formatted(value));
    }

    private void ensureConnected() throws Exception {
        if (client == null || !client.isConnected()) {
            closeQuietly();
            client = new ModbusTcpClient(host, port);
            client.connect();
        }
    }

    private void closeQuietly() {
        if (client != null) {
            client.close();
            client = null;
        }
    }

    private static String shortMsg(Exception e) {
        String m = e.getMessage();
        if (m == null) {
            return e.getClass().getSimpleName();
        }
        return m.length() > 80 ? m.substring(0, 80) : m;
    }

    public Snapshot snapshot() {
        return latest;
    }

    public void stop() {
        running = false;
        closeQuietly();
    }
}