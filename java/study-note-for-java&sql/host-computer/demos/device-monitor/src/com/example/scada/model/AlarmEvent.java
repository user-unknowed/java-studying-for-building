package com.example.scada.model;

import java.time.Instant;

/** 一条报警事件（不可变记录）。 */
public record AlarmEvent(Instant ts, String kind, String message, double value) {
}