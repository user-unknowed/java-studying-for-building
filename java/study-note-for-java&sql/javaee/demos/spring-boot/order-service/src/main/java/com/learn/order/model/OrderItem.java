package com.learn.order.model;

/**
 * 订单明细行（对应 order_item 表）
 *
 * <p>设计要点：把下单瞬间的「商品名 + 单价」快照进来。
 * 商品后续改名或调价，历史订单不受影响 —— 这是订单系统的经典设计。</p>
 */
public record OrderItem(long id, long orderId, long productId, String productName, long priceCents, int quantity) {
}
