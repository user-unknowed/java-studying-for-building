package com.learn.order.common;

/** 业务异常：业务规则不满足（如库存不足）→ 由全局异常处理器转成 400 响应 */
public class BizException extends RuntimeException {

    public BizException(String message) {
        super(message);
    }
}