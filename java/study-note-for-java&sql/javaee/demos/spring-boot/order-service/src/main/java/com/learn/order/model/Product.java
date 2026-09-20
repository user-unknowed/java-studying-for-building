package com.learn.order.model;

/**
 * 商品（对应 product 表）
 *
 * <p>金额统一用「分」存储（cents），彻底避免 double 浮点精度问题：
 * 0.1 + 0.2 != 0.3 的坑在订单/支付场景是致命的（相关演示见 JavaSE 章节）。</p>
 */
public record Product(long id, String name, long priceCents, int stock) {
}
