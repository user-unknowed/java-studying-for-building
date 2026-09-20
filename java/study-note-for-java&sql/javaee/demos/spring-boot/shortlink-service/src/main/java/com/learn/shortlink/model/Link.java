package com.learn.shortlink.model;

/** 短链（对应 link 表的一行） */
public record Link(long id, String code, String originalUrl, long clicks, String createdAt) {
}
