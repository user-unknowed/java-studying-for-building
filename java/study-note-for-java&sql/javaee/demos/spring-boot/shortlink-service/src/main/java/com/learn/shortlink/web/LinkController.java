package com.learn.shortlink.web;

import com.learn.shortlink.model.CreateLinkRequest;
import com.learn.shortlink.model.Link;
import com.learn.shortlink.service.ShortlinkService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 表现层：创建短链、查看统计、核心的「短码跳转」。
 *
 * <p>跳转用 302（Found）而非 301（Moved Permanently）：
 * 301 会被浏览器长期缓存，之后用户再访问短码可能不再回源，
 * 点击数就统计不准了 —— 短链服务必须用 302。这就是一个
 * 「HTTP 语义细节直接影响业务统计」的真实案例。</p>
 */
@RestController
public class LinkController {

    private final ShortlinkService service;

    /** 用于拼出完整短链地址（演示用；生产环境应由域名配置决定） */
    @Value("${server.port}")
    private int port;

    public LinkController(ShortlinkService service) {
        this.service = service;
    }

    /** 创建短链 */
    @PostMapping("/api/links")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@Valid @RequestBody CreateLinkRequest req) {
        Link link = service.create(req);
        return Map.of(
                "code", link.code(),
                "shortUrl", "http://localhost:" + port + "/" + link.code(),
                "originalUrl", link.originalUrl());
    }

    /** 短链列表（最近 20 条） */
    @GetMapping("/api/links")
    public List<Link> list() {
        return service.listRecent();
    }

    /** 短码统计（点击数等） */
    @GetMapping("/api/links/{code}")
    public Link stats(@PathVariable String code) {
        return service.stats(code);
    }

    /** 核心接口：访问短码 → 302 跳转到原始 URL（并累加点击） */
    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String target = service.resolve(code);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", target)
                .build();
    }
}