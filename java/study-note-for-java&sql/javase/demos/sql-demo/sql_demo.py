# -*- coding: utf-8 -*-
"""
SQL 学习演示脚本 —— 用 Python 内置 sqlite3 真实运行"工业界常用查询"
运行方式: python3 sql_demo.py
说明: SQLite 与 MySQL/PostgreSQL 在语法上高度相似(窗口函数/CTE/EXPLAIN 均有),
      本脚本用于快速验证与练习; 生产环境请使用 MySQL/PostgreSQL。
"""
import sqlite3

print('Python SQLite 版本:', sqlite3.sqlite_version)
conn = sqlite3.connect(':memory:')  # 内存数据库, 进程退出即销毁
cur = conn.cursor()

# ========== 1. DDL: 建表 ==========
cur.executescript("""
CREATE TABLE dept (
    id   INTEGER PRIMARY KEY,
    name TEXT NOT NULL
);
CREATE TABLE emp (
    id        INTEGER PRIMARY KEY,
    name      TEXT NOT NULL,
    dept_id   INTEGER,
    salary    REAL,
    hire_date TEXT,
    mgr_id    INTEGER          -- 直属经理 id, 自关联
);
CREATE TABLE orders (
    id         INTEGER PRIMARY KEY,
    user_id    INTEGER,
    amount     REAL,
    order_date TEXT
);
CREATE TABLE login_log (
    user_id    INTEGER,
    login_date TEXT
);
""")

# ========== 2. DML: 插入数据 ==========
cur.executemany('INSERT INTO dept VALUES (?,?)', [
    (1, '研发部'), (2, '市场部'), (3, '财务部')])
cur.executemany('INSERT INTO emp VALUES (?,?,?,?,?,?)', [
    (1, '张伟', 1, 28000, '2018-03-01', None),
    (2, '李娜', 1, 22000, '2019-07-15', 1),
    (3, '王强', 1, 18000, '2021-01-10', 1),
    (4, '赵敏', 2, 20000, '2020-05-20', None),
    (5, '刘洋', 2, 20000, '2022-09-01', 4),
    (6, '陈静', 3, 17000, '2019-11-11', None),
    (7, '杨帆', 1, 30000, '2016-02-01', None),
])
cur.executemany('INSERT INTO orders (user_id, amount, order_date) VALUES (?,?,?)', [
    (1, 100, '2026-01-05'), (1, 250, '2026-02-14'), (2, 80, '2026-01-20'),
    (3, 400, '2026-03-02'), (2, 120, '2026-02-22'), (1, 90, '2026-03-30'),
])
cur.executemany('INSERT INTO login_log VALUES (?,?)', [
    (1, '2026-03-01'), (1, '2026-03-02'), (1, '2026-03-03'), (1, '2026-03-05'),
    (2, '2026-03-01'), (2, '2026-03-03'), (2, '2026-03-04'),
])
conn.commit()


def show(no, title, sql, rows):
    print('=' * 68)
    print(f'【{no}】{title}')
    print('-' * 68)
    print(sql.strip())
    print('· · · 执行结果 · · ·')
    for r in rows:
        print('   ', r)
    print()


# ========== 3. 多表 JOIN(含自连接) ==========
sql = """
SELECT e.name AS 员工, d.name AS 部门, e.salary AS 薪资,
       COALESCE(m.name, '—') AS 经理
FROM emp e
JOIN dept d ON e.dept_id = d.id      -- 内连接: 只保留有部门的员工
LEFT JOIN emp m ON e.mgr_id = m.id   -- 左连接 + 自连接: 没有经理也保留
ORDER BY d.id, e.salary DESC
"""
show(1, '多表 JOIN(内连接 + 左连接 + 自连接)', sql, cur.execute(sql).fetchall())

# ========== 4. GROUP BY + HAVING ==========
sql = """
SELECT d.name AS 部门, COUNT(*) AS 人数, ROUND(AVG(e.salary)) AS 平均薪资
FROM emp e JOIN dept d ON e.dept_id = d.id
GROUP BY d.id, d.name
HAVING AVG(e.salary) > 18000   -- 聚合后的过滤必须用 HAVING
ORDER BY 平均薪资 DESC
"""
show(2, 'GROUP BY + HAVING: 平均薪资超过 18000 的部门', sql, cur.execute(sql).fetchall())

# ========== 5. 窗口函数: ROW_NUMBER vs RANK ==========
sql = """
SELECT name AS 员工, dept_id AS 部门, salary AS 薪资,
       ROW_NUMBER() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS 行号,
       RANK()       OVER (PARTITION BY dept_id ORDER BY salary DESC) AS 排名
FROM emp
"""
show(3, '窗口函数: ROW_NUMBER(不重复) vs RANK(并列同名次, 注意市场部)', sql, cur.execute(sql).fetchall())

# ========== 6. 窗口函数: TopN per group ==========
sql = """
SELECT * FROM (
    SELECT name AS 员工, dept_id AS 部门, salary AS 薪资,
           ROW_NUMBER() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS rn
    FROM emp
) t
WHERE rn <= 2          -- 每个部门薪资前 2 名
ORDER BY 部门, rn
"""
show(4, '窗口函数实战: 每个部门薪资 Top2(分组排名模板)', sql, cur.execute(sql).fetchall())

# ========== 7. 窗口函数: 累计消费 ==========
sql = """
SELECT user_id AS 用户, order_date AS 日期, amount AS 金额,
       SUM(amount) OVER (PARTITION BY user_id ORDER BY order_date) AS 累计消费
FROM orders
ORDER BY user_id, order_date
"""
show(5, '窗口函数实战: 每个用户的累计消费(默认帧=首行到当前行)', sql, cur.execute(sql).fetchall())

# ========== 8. 经典面试题: 连续 3 天登录 ==========
sql = """
SELECT user_id AS 用户, MIN(login_date) AS 连续起始日, COUNT(*) AS 连续天数
FROM (
    SELECT user_id, login_date,
           DATE(login_date, '-' || ROW_NUMBER() OVER
                (PARTITION BY user_id ORDER BY login_date) || ' day') AS grp
    -- 日期 - 行号 = 常数 -> 连续日期落入同一个 grp 组
    FROM (SELECT DISTINCT user_id, login_date FROM login_log)
) t
GROUP BY user_id, grp
HAVING COUNT(*) >= 3
"""
show(6, '经典面试题: 连续 3 天登录的用户(日期-行号分组法)', sql, cur.execute(sql).fetchall())

# ========== 9. 行转列(Pivot) ==========
sql = """
SELECT user_id AS 用户,
       SUM(CASE WHEN order_date LIKE '2026-01%' THEN amount ELSE 0 END) AS "1月",
       SUM(CASE WHEN order_date LIKE '2026-02%' THEN amount ELSE 0 END) AS "2月",
       SUM(CASE WHEN order_date LIKE '2026-03%' THEN amount ELSE 0 END) AS "3月"
FROM orders
GROUP BY user_id
"""
show(7, '行转列: 每个用户每月消费额(CASE WHEN 透视)', sql, cur.execute(sql).fetchall())

# ========== 10. 递归 CTE: 组织架构树 ==========
sql = """
WITH RECURSIVE tree(id, name, lvl, path) AS (
    SELECT id, name, 1, name
    FROM emp WHERE mgr_id IS NULL            -- 递归起点: 没有经理的根节点
    UNION ALL
    SELECT e.id, e.name, t.lvl + 1, t.path || ' → ' || e.name
    FROM emp e JOIN tree t ON e.mgr_id = t.id -- 递归步: 把自己挂到上级下面
)
SELECT lvl AS 层级, path AS 汇报链
FROM tree ORDER BY path
"""
show(8, '递归 CTE: 组织架构树(汇报链)', sql, cur.execute(sql).fetchall())

# ========== 11. EXPLAIN QUERY PLAN: 索引效果 ==========
print('=' * 68)
print('【9】EXPLAIN QUERY PLAN: 加索引前 vs 加索引后')
print('-' * 68)
before = cur.execute('EXPLAIN QUERY PLAN SELECT * FROM orders WHERE user_id = 1').fetchall()
print('加索引前:', [r[-1] for r in before])
cur.execute('CREATE INDEX idx_orders_user ON orders(user_id)')
after = cur.execute('EXPLAIN QUERY PLAN SELECT * FROM orders WHERE user_id = 1').fetchall()
print('加索引后:', [r[-1] for r in after])
print()
print('演示结束 —— 完整讲解见《SQL数据库深度讲解-工业实践版.md》')
