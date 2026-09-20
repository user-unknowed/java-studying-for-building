package com.learn.order.model;

/** 订单头（对应 orders 表） */
public record Order(long id, String userName, long totalCents, String status, String createdAt) {
}
