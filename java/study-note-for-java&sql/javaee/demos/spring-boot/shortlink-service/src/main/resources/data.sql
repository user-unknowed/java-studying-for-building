-- 演示数据（INSERT OR IGNORE：重复启动不重复插入）
INSERT OR IGNORE INTO link(code, original_url, clicks) VALUES
  ('gh',     'https://github.com/user-unknowed/java-studying-for-building', 0),
  ('sqlite', 'https://sqlite.org', 0);