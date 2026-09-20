-- 短链接服务 · 建表脚本

CREATE TABLE IF NOT EXISTS link (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    code         TEXT    NOT NULL UNIQUE,          -- 短码唯一：数据库层的最后防线
    original_url TEXT    NOT NULL,
    clicks       INTEGER NOT NULL DEFAULT 0 CHECK (clicks >= 0),
    created_at   TEXT    NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- 说明：UNIQUE 约束会自动创建索引，短码查找（WHERE code = ?）无需再建重复索引。
