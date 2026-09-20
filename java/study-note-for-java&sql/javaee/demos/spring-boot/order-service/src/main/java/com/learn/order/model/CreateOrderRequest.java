package com.learn.order.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 下单请求体（DTO）
 *
 * <p>record + Bean Validation：参数校验由框架在进入 Controller 前自动完成，
 * 业务代码里不再出现一堆 if (xx == null) 之类的「防御性垃圾代码」。</p>
 */
public record CreateOrderRequest(
        @NotBlank(message = "userName 不能为空")
        String userName,

        @NotEmpty(message = "items 不能为空")
        @Valid
        List<Item> items
) {
    /** 订单行：买哪个商品、买几件 */
    public record Item(
            @NotNull(message = "productId 不能为空")
            Long productId,

            @Min(value = 1, message = "quantity 至少为 1")
            int quantity
    ) {}
}
