package com.example.webmonitor.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 一次设备读数（record 不可变数据类）—— Web 版上位机的"数据事实"。
 *
 * <p>与 06 号教程的 DeviceReading 同构：一次采样 = 一条不可变记录，
 * 天然适合历史库追加与多线程间安全传递。
 */
public record Reading(
        Instant ts,        // 采样时刻
        double temp,       // 温度（℃）
        double humidity,   // 湿度（%）
        int light,         // 光照（lux）
        double soil,       // 土壤湿度（%）
        double limit,      // 当前报警上限（℃）
        boolean alarm) {   // 是否处于报警状态

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    /** HH:mm:ss 格式的时间串（界面显示用）。 */
    public String timeStr() {
        return FMT.format(ts);
    }
}