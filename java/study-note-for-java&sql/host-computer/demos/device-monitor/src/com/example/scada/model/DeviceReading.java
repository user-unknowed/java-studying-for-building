package com.example.scada.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 一次设备读数 —— record（Java 16+ 不可变数据类）教学示例。
 *
 * <p>写一行 record = 全字段构造器 + 访问器（ts() / tempC() ...）
 * + equals / hashCode / toString。工控场景非常合适：
 * 一次采样，就是一个"不可变的事实记录"。
 */
public record DeviceReading(
        Instant ts,       // 采样时刻
        double tempC,     // 温度（℃）
        double humidity,  // 湿度（%）
        int light,        // 光照（lux）
        double soilPct,   // 土壤湿度（%）
        double maxTempC,  // 当前报警上限（℃）
        boolean alarm) {  // 是否处于报警状态

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    /** HH:mm:ss 格式的时间串（界面显示用）。 */
    public String timeStr() {
        return FMT.format(ts);
    }
}