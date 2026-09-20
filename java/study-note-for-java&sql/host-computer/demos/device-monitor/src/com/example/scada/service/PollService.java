package com.example.scada.service;

import com.example.scada.db.MonitorDb;
import com.example.scada.model.AlarmEvent;
import com.example.scada.model.DeviceReading;
import com.example.scada.protocol.ModbusException;

import java.sql.SQLException;
import java.time.Instant;

/**
 * 轮询采集服务：读寄存器 → 解析物理量 → 入库 → 报警判断。
 *
 * <p>与"从哪读"解耦：构造时传入一个 {@link RegisterReader}（函数式接口）。
 * TCP 场景传 ModbusTcpClient 的读取方法，串口场景传 ModbusSerialClient 的——
 * 服务本身不知道也不关心底层链路。
 */
public class PollService {

    /** 寄存器读取函数式接口（可用方法引用 / lambda 传入）。 */
    @FunctionalInterface
    public interface RegisterReader {
        int[] read(int start, int count) throws ModbusException;
    }

    private final RegisterReader reader;
    private final MonitorDb db;
    private boolean lastAlarm = false;

    public PollService(RegisterReader reader, MonitorDb db) {
        this.reader = reader;
        this.db = db;
    }

    /** 采集一轮：读 4 个环境量（0~3）+ 4 个状态量（9~12），入库并返回读数。 */
    public DeviceReading pollOnce() throws ModbusException, SQLException {
        int[] env = reader.read(0, 4);
        int[] meta = reader.read(9, 4);

        var now = Instant.now();
        var reading = new DeviceReading(
                now,
                env[0] / 10.0,   // 温度 ×10 解码
                env[1] / 10.0,   // 湿度
                env[2],          // 光照
                env[3] / 10.0,   // 土壤
                meta[0] / 10.0,  // 报警上限
                meta[1] != 0);   // 报警标志

        db.insertReading(reading);

        // 报警"上升沿"检测：0→1 的瞬间记录一条报警（避免每轮刷屏）
        if (reading.alarm() && !lastAlarm) {
            String msg = "温度超上限: %.1f℃ > %.1f℃".formatted(reading.tempC(), reading.maxTempC());
            db.insertAlarm(new AlarmEvent(now, "TEMP_HIGH", msg, reading.tempC()));
        }
        lastAlarm = reading.alarm();

        return reading;
    }

    public boolean lastAlarmState() {
        return lastAlarm;
    }
}