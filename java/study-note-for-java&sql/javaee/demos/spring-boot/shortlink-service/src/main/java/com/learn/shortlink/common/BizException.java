package com.learn.shortlink.common;

import org.springframework.http.HttpStatus;

/**
 * 业务异常：携带期望的 HTTP 状态码（404 / 409 / 400 …）。
 *
 * <p>业务层只负责 throw；由全局异常处理器（ApiExceptionHandler）
 * 统一翻译成结构化 JSON 响应 —— 分层协作，业务代码零「样板错误处理」。</p>
 */
public class BizException extends RuntimeException {

    private final HttpStatus status;

    private BizException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public static BizException notFound(String message) {
        return new BizException(HttpStatus.NOT_FOUND, message);
    }

    public static BizException conflict(String message) {
        return new BizException(HttpStatus.CONFLICT, message);
    }

    public static BizException badRequest(String message) {
        return new BizException(HttpStatus.BAD_REQUEST, message);
    }

    public HttpStatus status() {
        return status;
    }
}
