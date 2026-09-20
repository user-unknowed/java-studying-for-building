package com.learn.order.model;

import java.util.List;

/** 订单详情：订单头 + 明细行（聚合 DTO，一次查询返回） */
public record OrderDetail(Order order, List<OrderItem> items) {
}
