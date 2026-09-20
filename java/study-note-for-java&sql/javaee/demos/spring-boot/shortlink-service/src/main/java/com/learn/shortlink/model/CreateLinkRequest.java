package com.learn.shortlink.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 创建短链请求体（DTO）
 *
 * <p>customCode 可选：为 null 时由系统自动生成短码；
 * 填写时必须符合正则（字母/数字/下划线/连字符，1~16 位）。
 * 注意：Bean Validation 中 null 值默认视为「合法」——正好适合可选字段的场景。</p>
 */
public record CreateLinkRequest(
        @NotBlank(message = "originalUrl 不能为空")
        String originalUrl,

        @Pattern(regexp = "^[A-Za-z0-9_-]{1,16}$",
                message = "customCode 只能包含字母/数字/下划线/连字符，长度 1~16")
        String customCode
) {
}
