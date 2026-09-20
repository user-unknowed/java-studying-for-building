package com.example.scada.ui;

import com.example.scada.model.DeviceReading;

/** 控制台监控面板：把每轮读数格式化为固定宽度的一行。 */
public final class ConsoleDashboard {

    private ConsoleDashboard() {
    }

    public static void header(String linkDesc, int intervalSec) {
        System.out.println("═".repeat(72));
        System.out.println("设备环境监控中 · " + linkDesc + " · 轮询间隔 " + intervalSec + "s");
        System.out.println("═".repeat(72));
        System.out.printf("%-8s │ %8s │ %7s │ %8s │ %7s │ %s%n",
                "时间", "温度℃", "湿度%", "光照lx", "土壤%", "状态");
        System.out.println("─".repeat(72));
    }

    public static void line(DeviceReading r) {
        String status = r.alarm()
                ? "⚠ 超上限(>" + String.format("%.1f", r.maxTempC()) + "℃)"
                : "正常(上限 " + String.format("%.1f", r.maxTempC()) + "℃)";
        System.out.printf("%-8s │ %8.1f │ %7.1f │ %8d │ %7.1f │ %s%n",
                r.timeStr(), r.tempC(), r.humidity(), r.light(), r.soilPct(), status);
    }
}