package com.learn.shortlink.service;

import com.learn.shortlink.common.BizException;
import com.learn.shortlink.model.CreateLinkRequest;
import com.learn.shortlink.model.Link;
import com.learn.shortlink.repo.LinkRepository;
import java.security.SecureRandom;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 业务层：短码生成、冲突处理、点击统计。
 *
 * <p>短码方案：Base62 字符表 + SecureRandom 随机生成 7 位
 * （62^7 ≈ 3.5 万亿种组合）。冲突概率极低，但仍以
 * 「数据库唯一约束 + 重试」兜底 —— 永远不要假设「概率低」等于「不会发生」。</p>
 */
@Service
public class ShortlinkService {

    private static final String ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CODE_LENGTH = 7;
    private static final int MAX_RETRY = 5;

    private final LinkRepository links;

    public ShortlinkService(LinkRepository links) {
        this.links = links;
    }

    /** 创建短链（自定义短码或自动生成） */
    @Transactional
    public Link create(CreateLinkRequest req) {
        String url = normalize(req.originalUrl());

        // ① 用户指定了自定义短码：先查重，重复直接 409
        String custom = req.customCode() == null ? null : req.customCode().trim();
        if (custom != null && !custom.isEmpty()) {
            if (links.existsByCode(custom)) {
                throw BizException.conflict("短码已被占用: " + custom);
            }
            links.insert(custom, url);
            return links.findByCode(custom).orElseThrow();
        }

        // ② 自动生成：随机短码 + 冲突重试（数据库唯一约束是最后一道防线）
        for (int i = 0; i < MAX_RETRY; i++) {
            String code = randomCode();
            if (links.existsByCode(code)) {
                continue; // 小概率撞码，换一个再试
            }
            try {
                links.insert(code, url);
                return links.findByCode(code).orElseThrow();
            } catch (Exception e) {
                // 极端并发下两个请求同时通过了查重 → 让唯一约束兜底，重试即可
            }
        }
        throw BizException.badRequest("短码生成失败，请稍后重试");
    }

    /** 解析短码：返回原始 URL 并原子累加点击；不存在 → 404 */
    @Transactional
    public String resolve(String code) {
        Link link = links.findByCode(code)
                .orElseThrow(() -> BizException.notFound("短码不存在: " + code));
        links.incrementClicks(code);
        return link.originalUrl();
    }

    /** 短码统计信息；不存在 → 404 */
    public Link stats(String code) {
        return links.findByCode(code)
                .orElseThrow(() -> BizException.notFound("短码不存在: " + code));
    }

    /** 最近短链列表 */
    public List<Link> listRecent() {
        return links.findRecent(20);
    }

    /** 自动补全协议头，防止用户输入 "example.com" 这种裸域名 */
    private String normalize(String url) {
        String u = url.trim();
        if (!u.startsWith("http://") && !u.startsWith("https://")) {
            u = "https://" + u;
        }
        return u;
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}