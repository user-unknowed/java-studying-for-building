package com.learn.shortlink.web;

import com.learn.shortlink.common.BizException;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：业务异常携带状态码 → 统一翻译成结构化 JSON。
 *
 * <p>对比原生 Servlet 版（每个接口手写 try-catch 拼错误 JSON）：
 * 这里业务代码只管 throw，错误响应格式全项目统一。</p>
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** 业务异常：按异常自带的状态码返回（404 / 409 / 400 …） */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<Map<String, Object>> biz(BizException e) {
        return ResponseEntity.status(e.status())
                .body(Map.of("error", e.getMessage(), "type", "BIZ_ERROR"));
    }

    /** 参数校验失败 → 400（列出全部字段错误） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> validation(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return Map.of("error", detail, "type", "VALIDATION_ERROR");
    }
}