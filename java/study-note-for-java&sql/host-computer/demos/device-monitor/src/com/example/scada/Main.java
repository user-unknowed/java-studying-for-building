package com.example.scada;

import com.example.scada.db.MonitorDb;
import com.example.scada.link.JscSerialTransport;
import com.example.scada.link.PtyFileTransport;
import com.example.scada.link.SerialTransport;
import com.example.scada.protocol.ModbusException;
import com.example.scada.protocol.ModbusSerialClient;
import com.example.scada.protocol.ModbusTcpClient;
import com.example.scada.service.PollService;
import com.example.scada.ui.ConsoleDashboard;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DeviceMonitor 上位机入口。
 *
 * <pre>
 * 子命令：
 *   monitor  采集监控   [--tcp host:port | --serial /dev/xxx] [--interval 2] [--count N] [--db data/monitor.db]
 *   report   读取报表   [--db data/monitor.db]
 *   control  下发控制   [--tcp host:port | --serial /dev/xxx] [--max-temp 28.0]
 *   gui      启动 Swing 监控窗口（PC 桌面环境）
 * </pre>
 */
public class Main {

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                usage();
                return;
            }
            switch (args[0]) {
                case "monitor" -> runMonitor(args);
                case "report" -> runReport(args);
                case "control" -> runControl(args);
                case "gui" -> runGui(args);
                default -> usage();
            }
        } catch (Exception e) {
            System.err.println("[FATAL] " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void runMonitor(String[] args) throws Exception {
        Map<String, String> opts = parse(args);
        int intervalSec = Integer.parseInt(opts.getOrDefault("interval", "2"));
        int count = Integer.parseInt(opts.getOrDefault("count", "0")); // 0 = 不限
        String dbFile = opts.getOrDefault("db", "data/monitor.db");

        try (MonitorDb db = new MonitorDb(dbFile);
             ClientSource source = sourceFor(opts)) {
            PollService service = new PollService(source.reader, db);
            ConsoleDashboard.header(describeTarget(opts), intervalSec);

            int i = 0;
            while (count == 0 || i < count) {
                try {
                    var reading = service.pollOnce();
                    ConsoleDashboard.line(reading);
                } catch (ModbusException e) {
                    // 通信失败不退出：打印警告，下轮重试（工控软件要有"韧性"）
                    System.out.println("[WARN] 采集失败: " + e.getMessage() + "（下轮重试）");
                }
                i++;
                if (count == 0 || i < count) {
                    Thread.sleep(intervalSec * 1000L);
                }
            }
            System.out.println("[monitor] 采集结束，共 " + i + " 轮");
        }
    }

    private static void runReport(String[] args) throws Exception {
        Map<String, String> opts = parse(args);
        try (MonitorDb db = new MonitorDb(opts.getOrDefault("db", "data/monitor.db"))) {
            System.out.println(db.report());
        }
    }

    private static void runControl(String[] args) throws Exception {
        Map<String, String> opts = parse(args);
        double maxTemp = Double.parseDouble(opts.getOrDefault("max-temp", "28.0"));
        int raw = (int) Math.round(maxTemp * 10);

        if (opts.containsKey("tcp")) {
            String[] hp = opts.get("tcp").split(":");
            try (ModbusTcpClient client = new ModbusTcpClient(hp[0], Integer.parseInt(hp[1]))) {
                client.connect();
                client.writeSingleRegister(9, raw);
            }
        } else if (opts.containsKey("serial")) {
            try (ModbusSerialClient client = serialClientFor(opts)) {
                client.writeSingleRegister(9, raw);
            }
        } else {
            throw new IllegalArgumentException("需要 --tcp host:port 或 --serial 设备路径");
        }

        try (MonitorDb db = new MonitorDb(opts.getOrDefault("db", "data/monitor.db"))) {
            db.logOp("SET_MAX_TEMP", "max_temp=" + maxTemp);
        }
        System.out.printf("[control] 已下发报警上限 %.1f℃（写寄存器 9 ← %d）%n", maxTemp, raw);
    }

    private static void runGui(String[] args) {
        Map<String, String> opts = parse(args);
        com.example.scada.ui.MonitorFrame.launch(opts.getOrDefault("db", "data/monitor.db"));
    }

    // ---------- 工具方法 ----------

    /** 把"客户端"和"读取函数"打包，便于 try-with-resources 统一关闭。 */
    private static final class ClientSource implements AutoCloseable {
        final PollService.RegisterReader reader;
        private final AutoCloseable closable;

        ClientSource(PollService.RegisterReader reader, AutoCloseable closable) {
            this.reader = reader;
            this.closable = closable;
        }

        @Override
        public void close() throws Exception {
            closable.close();
        }
    }

    private static ClientSource sourceFor(Map<String, String> opts) throws Exception {
        if (opts.containsKey("tcp")) {
            String[] hp = opts.get("tcp").split(":");
            var client = new ModbusTcpClient(hp[0], Integer.parseInt(hp[1]));
            client.connect();
            return new ClientSource(client::readHoldingRegisters, client);
        }
        if (opts.containsKey("serial")) {
            var client = serialClientFor(opts);
            return new ClientSource(client::readHoldingRegisters, client);
        }
        throw new IllegalArgumentException("需要 --tcp host:port 或 --serial 设备路径");
    }

    private static ModbusSerialClient serialClientFor(Map<String, String> opts) throws Exception {
        String path = opts.get("serial");
        int baud = Integer.parseInt(opts.getOrDefault("baud", "9600"));
        // PTY 虚拟串口用文件流版；真机设备用 jSerialComm（详见教程说明）
        SerialTransport t = path.startsWith("/dev/pts")
                ? new PtyFileTransport(path, baud)
                : new JscSerialTransport(path, baud);
        t.open();
        return new ModbusSerialClient(t, 1, 3000);
    }

    private static String describeTarget(Map<String, String> opts) {
        if (opts.containsKey("tcp")) {
            return "Modbus TCP " + opts.get("tcp");
        }
        if (opts.containsKey("serial")) {
            return "Modbus RTU " + opts.get("serial");
        }
        return "?";
    }

    /** 解析 --key value 形式的参数（教学简版）。 */
    private static Map<String, String> parse(String[] args) {
        var map = new LinkedHashMap<String, String>();
        for (int i = 1; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                String key = args[i].substring(2);
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    map.put(key, args[++i]);
                } else {
                    map.put(key, "true");
                }
            }
        }
        return map;
    }

    private static void usage() {
        System.out.println("""
                DeviceMonitor 上位机 · 用法：
                  monitor  [--tcp host:port | --serial /dev/xxx] [--interval 2] [--count N]
                  report   [--db data/monitor.db]
                  control  [--tcp host:port | --serial /dev/xxx] [--max-temp 28.0]
                  gui      [--db data/monitor.db]""");
    }
}