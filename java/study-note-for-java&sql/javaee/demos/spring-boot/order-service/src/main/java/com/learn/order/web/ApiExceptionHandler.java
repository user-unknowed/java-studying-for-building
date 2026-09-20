package com.learn.order.web;

import com.learn.order.common.BizException;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：把各类异常统一转换成结构化的 JSON 错误响应。
 *
 * <p>业务代码只管 throw，不需要在每个接口里 try-catch 写「错误返回」样板代码。</p>
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** 业务异常 → 400 */
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> biz(BizException e) {
        return Map.of("error", e.getMessage(), "type", "BIZ_ERROR");
    }

    /** 参数校验失败 → 400（把字段级错误全部列出来） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> validation(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Map.of("error", detail, "type", "VALIDATION_ERROR");
    }
}