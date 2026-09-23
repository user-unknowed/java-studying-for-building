#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
构建教程网站（单文件 HTML，可离线打开）。
读取 /workspace/docs-src/ 下的 7 份 markdown，嵌入一个轻量 markdown 渲染器，
生成 /workspace/studying-for-building-教程/index.html。
"""

import json
import os
import re

SRC_DIR = "/workspace/docs-src"
OUT_DIR = "/workspace/studying-for-building-教程"
OUT_FILE = os.path.join(OUT_DIR, "index.html")

# 文档元信息：按学习路线递进
# (id, 文件名, 侧边栏显示名, 阶段分组)
DOCS = [
    ("01", "01-JavaSE基础知识点-完整梳理.md",         "01 JavaSE 基础知识点",   "第一阶梯：JavaSE 地基"),
    ("02", "02-Java与SQL数据库-深度融合教学.md",      "02 Java 与 SQL 数据库融合", "第二阶梯：Java × SQL"),
    ("03", "03-SQL数据库深度讲解-工业实践版.md",       "03 SQL 数据库深度讲解",   "第二阶梯：Java × SQL"),
    ("04", "04-学习报告-Java-数据库-AI-Agent.md",     "04 Java·数据库·AI Agent", "学习报告"),
    ("05", "05-JavaEE企业级开发-现代实战教程.md",      "05 JavaEE 企业级开发",    "第三阶梯：JavaEE 服务端"),
    ("06", "06-Java上位机开发实战-串口Modbus数据采集与监控.md", "06 Java 上位机开发实战", "第四阶梯：上位机"),
    ("07", "07-JavaEE上位机实战-网页监控看板与远程控制.md",     "07 JavaEE 上位机实战",   "第四阶梯：上位机"),
]

# 阶段分组顺序
STAGE_ORDER = [
    "第一阶梯：JavaSE 地基",
    "第二阶梯：Java × SQL",
    "学习报告",
    "第三阶梯：JavaEE 服务端",
    "第四阶梯：上位机",
]


def read_docs():
    """读取所有 markdown 文件，返回 {id: content} 字典。"""
    docs = {}
    for doc_id, fname, _title, _stage in DOCS:
        path = os.path.join(SRC_DIR, fname)
        with open(path, "r", encoding="utf-8") as f:
            docs[doc_id] = f.read()
    return docs


def build_sidebar_html():
    """构建侧边栏导航 HTML（首页 + 分组文档）。"""
    parts = []
    # 首页
    parts.append(
        '<a class="nav-item nav-home" href="#page-home" data-page="home">'
        '<span class="nav-icon">🏠</span> 首页</a>'
    )

    # 按 STAGE_ORDER 分组
    grouped = {s: [] for s in STAGE_ORDER}
    for doc_id, _fname, title, stage in DOCS:
        grouped[stage].append((doc_id, title))

    for stage in STAGE_ORDER:
        items = grouped[stage]
        if not items:
            continue
        parts.append(
            f'<div class="nav-stage">{stage}</div>'
        )
        for doc_id, title in items:
            parts.append(
                f'<a class="nav-item" href="#page-{doc_id}" data-page="{doc_id}">{title}</a>'
            )

    return "\n".join(parts)


HOMEPAGE_HTML = """
<div class="home-hero">
  <div class="home-badge">📚 系统化学习教程</div>
  <h1 class="home-title">Java / SQL 系统化学习教程</h1>
  <p class="home-subtitle">从 JavaSE 基础 → Java×SQL 融合 → SQL 工业实践 → JavaEE 服务端开发 → Java 上位机</p>
  <div class="home-meta">
    <span>📦 7 篇文档</span>
    <span>📂 5 个学习阶梯</span>
    <span>🛠 可离线运行</span>
    <span>🗓 整理于 2026-09</span>
  </div>
</div>

<div class="home-section">
  <h2 class="home-section-title">🗺 五阶梯学习路线图</h2>
  <div class="roadmap">
    <div class="roadmap-step">
      <div class="roadmap-num">1</div>
      <div class="roadmap-body">
        <div class="roadmap-name">JavaSE 地基</div>
        <div class="roadmap-desc">运行机制、类型系统、OOP、集合、异常、IO、多线程、反射、现代语法、网络编程</div>
      </div>
    </div>
    <div class="roadmap-arrow">↓</div>
    <div class="roadmap-step">
      <div class="roadmap-num">2</div>
      <div class="roadmap-body">
        <div class="roadmap-name">Java × SQL 融合</div>
        <div class="roadmap-desc">JDBC 六步、SQL 注入防御、事务、批处理；表↔对象映射心智模型</div>
      </div>
    </div>
    <div class="roadmap-arrow">↓</div>
    <div class="roadmap-step">
      <div class="roadmap-num">3</div>
      <div class="roadmap-body">
        <div class="roadmap-name">SQL 工业实践</div>
        <div class="roadmap-desc">关系模型、查询执行、索引、事务并发（MVCC/锁）、架构演进</div>
      </div>
    </div>
    <div class="roadmap-arrow">↓</div>
    <div class="roadmap-step">
      <div class="roadmap-num">4</div>
      <div class="roadmap-body">
        <div class="roadmap-name">JavaEE 服务端</div>
        <div class="roadmap-desc">Servlet、DAO、REST、Spring Boot 现代实战</div>
      </div>
    </div>
    <div class="roadmap-arrow">↓</div>
    <div class="roadmap-step">
      <div class="roadmap-num">5</div>
      <div class="roadmap-body">
        <div class="roadmap-name">Java 上位机</div>
        <div class="roadmap-desc">串口/Modbus 数据采集 → 网页监控看板与远程控制</div>
      </div>
    </div>
  </div>
</div>

<div class="home-section">
  <h2 class="home-section-title">📑 7 篇文档导航</h2>
  <p class="home-hint">点击任意卡片即可跳转阅读：</p>
  <div class="card-grid">
    <a class="doc-card" href="#page-01">
      <div class="card-stage">第一阶梯</div>
      <div class="card-title">01 JavaSE 基础知识点</div>
      <div class="card-desc">Java 地基：运行机制、类型、OOP、集合、异常、IO、多线程、反射、现代语法、网络编程</div>
    </a>
    <a class="doc-card" href="#page-02">
      <div class="card-stage">第二阶梯</div>
      <div class="card-title">02 Java 与 SQL 数据库融合</div>
      <div class="card-desc">JDBC、SQL 注入、事务、批处理</div>
    </a>
    <a class="doc-card" href="#page-03">
      <div class="card-stage">第二阶梯</div>
      <div class="card-title">03 SQL 数据库深度讲解</div>
      <div class="card-desc">关系模型、查询、索引、事务并发、架构</div>
    </a>
    <a class="doc-card" href="#page-04">
      <div class="card-stage">学习报告</div>
      <div class="card-title">04 Java·数据库·AI Agent</div>
      <div class="card-desc">三大方向知识地图与学习路径</div>
    </a>
    <a class="doc-card" href="#page-05">
      <div class="card-stage">第三阶梯</div>
      <div class="card-title">05 JavaEE 企业级开发</div>
      <div class="card-desc">Servlet、DAO、REST、Spring Boot</div>
    </a>
    <a class="doc-card" href="#page-06">
      <div class="card-stage">第四阶梯</div>
      <div class="card-title">06 Java 上位机开发实战</div>
      <div class="card-desc">串口、Modbus、数据采集与监控</div>
    </a>
    <a class="doc-card" href="#page-07">
      <div class="card-stage">第四阶梯</div>
      <div class="card-title">07 JavaEE 上位机实战</div>
      <div class="card-desc">网页版上位机：监控看板与远程控制</div>
    </a>
  </div>
</div>

<div class="home-section">
  <h2 class="home-section-title">🚀 可运行项目列表</h2>
  <p class="home-hint">教程配套的真实可编译运行项目（均在原文档中有完整代码）：</p>
  <ul class="project-list">
    <li><span class="proj-tag">JavaSE</span> 基础语法/OOP/集合/多线程/反射 示例集</li>
    <li><span class="proj-tag">Java×SQL</span> <code>java-sql-demo/</code> — JDBC 六步、事务、批处理真实运行</li>
    <li><span class="proj-tag">SQL</span> 索引/事务/MVCC 实验脚本</li>
    <li><span class="proj-tag">JavaEE</span> Servlet + DAO + REST + Spring Boot 服务端</li>
    <li><span class="proj-tag">上位机</span> 串口 Modbus 数据采集程序</li>
    <li><span class="proj-tag">上位机</span> 网页监控看板 + 远程控制系统</li>
  </ul>
</div>
"""


def build_html(docs):
    """组装最终 HTML 字符串。"""
    sidebar = build_sidebar_html()
    # JSON 序列化所有 markdown 内容，安全嵌入
    docs_json = json.dumps(docs, ensure_ascii=False)

    # 上一页/下一页的文档顺序（不含 home）
    doc_ids = [d[0] for d in DOCS]

    html = f"""<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Java / SQL 系统化学习教程</title>
<style>
:root {{
  --primary: #2563eb;
  --primary-light: #dbeafe;
  --primary-dark: #1d4ed8;
  --bg: #ffffff;
  --bg-soft: #f6f8fa;
  --bg-sidebar: #fbfbfc;
  --text: #1f2328;
  --text-soft: #57606a;
  --text-muted: #8b949e;
  --border: #d0d7de;
  --border-soft: #eaeef2;
  --code-bg: #1e2227;
  --code-text: #e6edf3;
  --code-inline-bg: #eff1f3;
  --code-inline-text: #d6336c;
  --quote-bg: #f6f8fa;
  --quote-border: #2563eb;
  --table-head-bg: #f0f3f7;
  --table-stripe: #f9fbfd;
  --shadow: 0 1px 3px rgba(0,0,0,0.06), 0 8px 24px rgba(0,0,0,0.04);
  --sidebar-w: 280px;
}}
* {{ box-sizing: border-box; }}
html, body {{ margin:0; padding:0; }}
body {{
  font-family: "PingFang SC", "Microsoft YaHei", "Hiragino Sans GB", "Source Han Sans SC",
               "Noto Sans CJK SC", "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  color: var(--text);
  background: var(--bg);
  line-height: 1.75;
  font-size: 15px;
  -webkit-font-smoothing: antialiased;
}}
a {{ color: var(--primary); text-decoration: none; }}
a:hover {{ text-decoration: underline; }}

/* ===== 顶部导航栏 ===== */
.topbar {{
  position: sticky; top:0; z-index: 50;
  height: 52px; display:flex; align-items:center; gap:12px;
  padding: 0 16px;
  background: rgba(255,255,255,0.92);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid var(--border-soft);
}}
.topbar .menu-toggle {{
  display:none; border:1px solid var(--border); background:#fff;
  border-radius:6px; padding:5px 9px; cursor:pointer; font-size:16px;
}}
.topbar .topbar-title {{ font-weight:600; color:var(--text); font-size:15px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }}
.topbar .spacer {{ flex:1; }}
.search-box {{
  position:relative; width:260px; max-width:40vw;
}}
.search-box input {{
  width:100%; height:32px; padding:0 12px 0 30px;
  border:1px solid var(--border); border-radius:6px;
  font-size:13px; outline:none; background:var(--bg-soft);
  font-family: inherit;
}}
.search-box input:focus {{ border-color: var(--primary); background:#fff; }}
.search-box::before {{
  content:"🔍"; position:absolute; left:8px; top:50%; transform:translateY(-50%);
  font-size:12px; opacity:0.6;
}}
.pager-btns {{ display:flex; gap:6px; }}
.pager-btns button {{
  height:32px; padding:0 12px; border:1px solid var(--border); background:#fff;
  border-radius:6px; cursor:pointer; font-size:13px; color:var(--text-soft);
  font-family: inherit; white-space:nowrap;
  transition: all .15s;
}}
.pager-btns button:hover:not(:disabled) {{ border-color: var(--primary); color: var(--primary); }}
.pager-btns button:disabled {{ opacity:0.4; cursor:not-allowed; }}

/* ===== 布局 ===== */
.layout {{ display:flex; align-items:flex-start; }}
.sidebar {{
  position: sticky; top:52px;
  width: var(--sidebar-w); height: calc(100vh - 52px);
  overflow-y:auto;
  background: var(--bg-sidebar);
  border-right: 1px solid var(--border-soft);
  padding: 18px 0 24px;
  flex-shrink:0;
}}
.sidebar::-webkit-scrollbar {{ width:6px; }}
.sidebar::-webkit-scrollbar-thumb {{ background:#d8dee4; border-radius:3px; }}
.site-title {{
  padding: 4px 20px 14px;
  font-size: 15px; font-weight:700; color: var(--primary);
  border-bottom: 1px solid var(--border-soft);
  margin-bottom: 8px;
}}
.nav-stage {{
  padding: 14px 20px 4px;
  font-size: 11px; font-weight:700; color: var(--text-muted);
  letter-spacing: 0.05em; text-transform: uppercase;
}}
.nav-item {{
  display:block; padding: 7px 20px 7px 22px;
  font-size: 13.5px; color: var(--text-soft);
  border-left: 3px solid transparent;
  transition: all .12s;
  line-height:1.5;
}}
.nav-item:hover {{ background: var(--bg-soft); color: var(--text); text-decoration:none; }}
.nav-item.active {{
  background: var(--primary-light);
  color: var(--primary-dark);
  border-left-color: var(--primary);
  font-weight:600;
}}
.nav-home {{ font-weight:600; color: var(--text); }}
.nav-icon {{ opacity:0.8; }}
.sidebar-footer {{
  margin-top:18px; padding: 12px 20px 4px;
  border-top: 1px solid var(--border-soft);
  font-size: 11.5px; color: var(--text-muted);
  line-height:1.7;
}}

.content {{
  flex:1; min-width:0;
  padding: 32px 48px 120px;
  max-width: 980px;
}}

/* ===== 页面切换 ===== */
.page {{ display:none; }}
.page.active {{ display:block; }}

/* ===== 返回顶部 ===== */
.back-to-top {{
  position: fixed; right: 28px; bottom: 28px;
  width: 42px; height:42px; border-radius:50%;
  background: var(--primary); color:#fff; border:none;
  font-size:20px; cursor:pointer; opacity:0; pointer-events:none;
  box-shadow: var(--shadow); transition: all .2s; z-index: 40;
}}
.back-to-top.show {{ opacity:1; pointer-events:auto; }}
.back-to-top:hover {{ background: var(--primary-dark); }}

/* ===== 文章页头 ===== */
.doc-header {{
  margin-bottom: 24px; padding-bottom: 16px;
  border-bottom: 1px solid var(--border-soft);
}}
.doc-header .doc-stage-tag {{
  display:inline-block; font-size:12px; padding:2px 10px;
  background: var(--primary-light); color: var(--primary-dark);
  border-radius: 10px; margin-bottom: 8px;
}}

/* ===== Markdown 渲染样式 ===== */
.markdown h1 {{ font-size: 28px; margin: 0 0 18px; padding-bottom: 12px; border-bottom:1px solid var(--border-soft); }}
.markdown h2 {{ font-size: 22px; margin: 36px 0 14px; padding-bottom:8px; border-bottom:1px solid var(--border-soft); }}
.markdown h3 {{ font-size: 18px; margin: 28px 0 12px; }}
.markdown h4 {{ font-size: 16px; margin: 22px 0 10px; }}
.markdown h5 {{ font-size: 15px; margin: 18px 0 8px; }}
.markdown h6 {{ font-size: 14px; margin: 16px 0 8px; color: var(--text-soft); }}
.markdown p {{ margin: 10px 0; }}
.markdown ul, .markdown ol {{ margin: 10px 0; padding-left: 26px; }}
.markdown li {{ margin: 4px 0; }}
.markdown li > ul, .markdown li > ol {{ margin: 4px 0; }}
.markdown a {{ color: var(--primary); border-bottom: 1px solid transparent; }}
.markdown a:hover {{ border-bottom-color: var(--primary); text-decoration:none; }}
.markdown strong {{ font-weight:700; }}
.markdown em {{ font-style: italic; }}
.markdown hr {{ border:none; border-top:1px solid var(--border-soft); margin: 26px 0; }}

.markdown blockquote {{
  margin: 14px 0; padding: 10px 16px;
  background: var(--quote-bg);
  border-left: 4px solid var(--quote-border);
  border-radius: 0 6px 6px 0;
  color: var(--text-soft);
}}
.markdown blockquote p {{ margin: 4px 0; }}
.markdown blockquote blockquote {{ margin: 8px 0; }}

/* 行内代码 */
.markdown code {{
  font-family: "JetBrains Mono", "Fira Code", "SF Mono", Consolas, "Liberation Mono", Menlo, monospace;
  font-size: 0.88em;
  background: var(--code-inline-bg);
  color: var(--code-inline-text);
  padding: 2px 6px; border-radius: 4px;
  word-break: break-word;
}}
/* 代码块 */
.markdown pre {{
  background: var(--code-bg);
  color: var(--code-text);
  padding: 16px 18px; border-radius: 8px;
  overflow-x: auto;
  margin: 14px 0;
  font-size: 13px; line-height: 1.6;
  border: 1px solid #0d1117;
}}
.markdown pre code {{
  background: transparent; color: inherit;
  padding: 0; border-radius:0; font-size: inherit;
  word-break: normal;
}}

/* 表格 */
.markdown table {{
  border-collapse: collapse; width: 100%;
  margin: 16px 0; font-size: 14px;
  overflow:hidden; border-radius: 6px;
}}
.markdown th, .markdown td {{
  border: 1px solid var(--border);
  padding: 8px 12px; text-align:left; vertical-align: top;
}}
.markdown th {{
  background: var(--table-head-bg); font-weight:600;
}}
.markdown tbody tr:nth-child(even) {{ background: var(--table-stripe); }}
.markdown tbody tr:hover {{ background: var(--primary-light); }}

/* ===== 首页样式 ===== */
.home-hero {{
  text-align:center; padding: 40px 20px 30px;
  background: linear-gradient(180deg, #f0f6ff 0%, #ffffff 100%);
  border-radius: 14px; margin-bottom: 36px;
}}
.home-badge {{
  display:inline-block; font-size:13px; color: var(--primary-dark);
  background: var(--primary-light); padding:4px 14px;
  border-radius: 14px; margin-bottom: 16px; font-weight:600;
}}
.home-title {{ font-size: 34px; margin:0 0 12px; color: var(--text); }}
.home-subtitle {{ font-size:16px; color: var(--text-soft); margin:0 auto 18px; max-width: 640px; }}
.home-meta {{ display:flex; flex-wrap:wrap; justify-content:center; gap:8px 18px; font-size:13px; color: var(--text-muted); }}
.home-section {{ margin: 32px 0; }}
.home-section-title {{
  font-size: 20px; margin: 0 0 14px; padding-bottom:8px;
  border-bottom: 2px solid var(--primary-light);
}}
.home-hint {{ color: var(--text-soft); font-size:14px; margin: 0 0 14px; }}

.roadmap {{ display:flex; flex-direction:column; align-items:stretch; gap:0; }}
.roadmap-step {{
  display:flex; align-items:flex-start; gap:14px;
  background: var(--bg-soft); border:1px solid var(--border-soft);
  border-radius:10px; padding:14px 18px;
}}
.roadmap-num {{
  flex-shrink:0; width:34px; height:34px; border-radius:50%;
  background: var(--primary); color:#fff; font-weight:700;
  display:flex; align-items:center; justify-content:center; font-size:16px;
}}
.roadmap-name {{ font-weight:700; font-size:15px; margin-bottom:2px; }}
.roadmap-desc {{ font-size:13px; color: var(--text-soft); }}
.roadmap-arrow {{ text-align:center; color: var(--primary); font-size:18px; padding:4px 0; }}

.card-grid {{ display:grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap:16px; }}
.doc-card {{
  display:block; padding:18px; border:1px solid var(--border-soft);
  border-radius:12px; background:#fff; transition: all .15s;
  color: var(--text);
}}
.doc-card:hover {{
  border-color: var(--primary); box-shadow: var(--shadow);
  transform: translateY(-2px); text-decoration:none;
}}
.card-stage {{
  font-size:11px; color: var(--primary-dark); background: var(--primary-light);
  display:inline-block; padding:2px 10px; border-radius:10px; margin-bottom:8px; font-weight:600;
}}
.card-title {{ font-size:16px; font-weight:700; margin-bottom:6px; }}
.card-desc {{ font-size:13px; color: var(--text-soft); line-height:1.6; }}

.project-list {{ list-style:none; padding:0; margin:0; }}
.project-list li {{
  padding:10px 14px; background: var(--bg-soft); border-radius:8px;
  margin-bottom:8px; font-size:14px; border-left:3px solid var(--primary);
}}
.proj-tag {{
  display:inline-block; font-size:11px; font-weight:700; color:#fff;
  background: var(--primary); padding:1px 8px; border-radius:4px; margin-right:8px;
}}
.project-list code {{ font-size:13px; }}

/* 搜索结果浮层 */
.search-results {{
  display:none; position:absolute; top:36px; right:0;
  width:320px; max-height: 60vh; overflow-y:auto;
  background:#fff; border:1px solid var(--border); border-radius:8px;
  box-shadow: var(--shadow); z-index:100; padding:6px;
}}
.search-results .sr-item {{
  display:block; padding:8px 10px; border-radius:6px; font-size:13px;
  color: var(--text); cursor:pointer;
}}
.search-results .sr-item:hover {{ background: var(--primary-light); text-decoration:none; }}
.search-results .sr-doc {{ font-size:11px; color: var(--text-muted); }}
.search-results .sr-empty {{ padding:14px; text-align:center; color: var(--text-muted); font-size:13px; }}

/* 移动端遮罩 */
.overlay {{
  display:none; position:fixed; inset:0; background:rgba(0,0,0,0.3);
  z-index:35;
}}
.overlay.show {{ display:block; }}

/* ===== 响应式 ===== */
@media (max-width: 900px) {{
  :root {{ --sidebar-w: 260px; }}
  .topbar .menu-toggle {{ display:inline-block; }}
  .search-box {{ width:180px; }}
  .sidebar {{
    position: fixed; top:52px; left:0; bottom:0; height:auto;
    z-index:36; transform: translateX(-100%); transition: transform .2s;
    box-shadow: var(--shadow);
  }}
  .sidebar.open {{ transform: translateX(0); }}
  .content {{ padding: 22px 18px 100px; }}
  .home-title {{ font-size: 26px; }}
  .card-grid {{ grid-template-columns: 1fr; }}
}}
@media (max-width: 520px) {{
  .pager-btns .btn-label {{ display:none; }}
  .search-box {{ width: 130px; }}
  .content {{ padding: 18px 14px 90px; }}
}}

@media print {{
  .sidebar, .topbar, .back-to-top, .overlay {{ display:none !important; }}
  .content {{ padding:0; max-width:100%; }}
  .page {{ display:block !important; }}
}}
</style>
</head>
<body>

<!-- 顶部导航栏 -->
<div class="topbar">
  <button class="menu-toggle" id="menuToggle" aria-label="菜单">☰</button>
  <div class="topbar-title" id="topbarTitle">Java / SQL 系统化学习教程</div>
  <div class="spacer"></div>
  <div class="search-box">
    <input type="text" id="searchInput" placeholder="搜索文档…" autocomplete="off">
    <div class="search-results" id="searchResults"></div>
  </div>
  <div class="pager-btns">
    <button id="prevBtn">← <span class="btn-label">上一页</span></button>
    <button id="nextBtn"><span class="btn-label">下一页</span> →</button>
  </div>
</div>

<div class="overlay" id="overlay"></div>

<div class="layout">
  <!-- 侧边栏 -->
  <aside class="sidebar" id="sidebar">
    <div class="site-title">📖 Java / SQL 学习教程</div>
    <nav id="nav">
{sidebar}
    </nav>
    <div class="sidebar-footer">
      整理：Operit AI · 2026-09<br>
      共 {len(DOCS)} 篇文档 · 离线可读
    </div>
  </aside>

  <!-- 内容区 -->
  <main class="content">
    <!-- 首页 -->
    <section class="page" id="page-home">
{HOMEPAGE_HTML}
    </section>
    <!-- 文档页：动态渲染 -->
    <section class="page" id="page-doc">
      <div class="doc-header">
        <span class="doc-stage-tag" id="docStageTag">文档</span>
        <div id="docTitle"></div>
      </div>
      <div class="markdown" id="docContent"></div>
    </section>
  </main>
</div>

<button class="back-to-top" id="backToTop" aria-label="返回顶部">↑</button>

<script id="docs-data" type="application/json">
{docs_json}
</script>

<script>
// 文档顺序与元信息
const DOC_ORDER = {json.dumps([list(d) for d in DOCS], ensure_ascii=False)};
const DOC_META = {{}};
DOC_ORDER.forEach(function(d){{ DOC_META[d[0]] = {{title:d[2], stage:d[3]}}; }});

// 读取嵌入的 markdown 数据
const DOCS = JSON.parse(document.getElementById('docs-data').textContent);

/* ============================================================
   轻量 Markdown 渲染器（纯 JS，无依赖）
   支持：h1-h6、段落、代码块```、行内代码`、表格|、列表-/*、
        引用>、粗体**、链接[]()、水平线---、有序列表1.
   ============================================================ */
function escapeHtml(s){{
  return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}}

// 行内渲染：先提取代码/链接，再转义 HTML，最后插入标签
function renderInlineSafe(text){{
  var codes = [];
  // 1. 提取行内代码
  text = text.replace(/`([^`]+)`/g, function(m, c){{
    codes.push(c);
    return '\\u0000C' + (codes.length-1) + '\\u0000';
  }});
  // 2. 提取链接 [text](url)
  var links = [];
  text = text.replace(/\\[([^\\]]+)\\]\\(([^)\\s]+)\\)/g, function(m, t, u){{
    links.push({{t:t, u:u}});
    return '\\u0000L' + (links.length-1) + '\\u0000';
  }});
  // 3. 转义 HTML
  text = escapeHtml(text);
  // 4. 粗体 **text**
  text = text.replace(/\\*\\*([^*]+)\\*\\*/g, function(m, c){{
    return '<strong>' + c + '</strong>';
  }});
  // 5. 斜体 *text*
  text = text.replace(/(^|[^*])\\*([^*\\n]+)\\*(?!\\*)/g, '$1<em>$2</em>');
  // 6. 还原链接
  text = text.replace(/\\u0000L(\\d+)\\u0000/g, function(m, i){{
    var l = links[+i];
    return '<a href="' + l.u + '" target="_blank" rel="noopener">' + escapeHtml(l.t) + '</a>';
  }});
  // 7. 还原行内代码
  text = text.replace(/\\u0000C(\\d+)\\u0000/g, function(m, i){{
    return '<code>' + escapeHtml(codes[+i]) + '</code>';
  }});
  return text;
}}

function renderMarkdown(md){{
  if(!md) return '';
  var lines = md.replace(/\\r\\n/g, '\\n').split('\\n');
  var html = [];
  var i = 0;
  var n = lines.length;

  // 代码块语言标记 -> class
  function langClass(lang){{
    lang = (lang||'').trim();
    return lang ? ' class="language-' + lang.replace(/[^a-z0-9+-]/gi,'') + '"' : '';
  }}

  while(i < n){{
    var line = lines[i];

    // --- 代码块 ---
    var fence = line.match(/^```\\s*(.*)$/);
    if(fence){{
      var lang = fence[1];
      var codeBuf = [];
      i++;
      while(i < n && !/^```\\s*$/.test(lines[i])){{
        codeBuf.push(lines[i]);
        i++;
      }}
      i++; // 跳过结束 ```
      html.push('<pre><code' + langClass(lang) + '>' + escapeHtml(codeBuf.join('\\n')) + '</code></pre>');
      continue;
    }}

    // --- 水平线 ---
    if(/^\\s*([-*_])\\1{{2,}}\\s*$/.test(line)){{
      html.push('<hr>');
      i++;
      continue;
    }}

    // --- 标题 ---
    var h = line.match(/^(#{{1,6}})\\s+(.*)$/);
    if(h){{
      var level = h[1].length;
      var txt = h[2].replace(/\\s+#+\\s*$/, '');
      html.push('<h' + level + '>' + renderInlineSafe(txt) + '</h' + level + '>');
      i++;
      continue;
    }}

    // --- 表格（连续以 | 开头的行，且第二行是分隔行）---
    if(/^\\s*\\|/.test(line) && i+1 < n && /^\\s*\\|?\\s*:?-{{2,}}/.test(lines[i+1])){{
      var tableRows = [];
      while(i < n && /^\\s*\\|/.test(lines[i])){{
        tableRows.push(lines[i]);
        i++;
      }}
      if(tableRows.length >= 2){{
        // 解析表头
        var header = splitTableRow(tableRows[0]);
        var body = tableRows.slice(2); // 跳过分隔行
        var t = '<table><thead><tr>';
        header.forEach(function(c){{ t += '<th>' + renderInlineSafe(c) + '</th>'; }});
        t += '</tr></thead><tbody>';
        body.forEach(function(r){{
          var cells = splitTableRow(r);
          t += '<tr>';
          // 补齐列数
          while(cells.length < header.length) cells.push('');
          cells.forEach(function(c){{ t += '<td>' + renderInlineSafe(c) + '</td>'; }});
          t += '</tr>';
        }});
        t += '</tbody></table>';
        html.push(t);
        continue;
      }}
    }}

    // --- 引用块 ---
    if(/^\\s*>/.test(line)){{
      var quoteBuf = [];
      while(i < n && /^\\s*>/.test(lines[i])){{
        quoteBuf.push(lines[i].replace(/^\\s*>\\s?/, ''));
        i++;
      }}
      html.push('<blockquote>' + renderMarkdown(quoteBuf.join('\\n')) + '</blockquote>');
      continue;
    }}

    // --- 列表（无序 - / * / +，有序 1. ）---
    var listMatch = line.match(/^(\\s*)([-*+]|\\d+\\.)\\s+(.*)$/);
    if(listMatch){{
      var isOrdered = /\\d+\\./.test(listMatch[2]);
      var listItems = [];
      // 收集整个列表块（含嵌套）
      var rawListBuf = [];
      while(i < n){{
        var lm = lines[i].match(/^(\\s*)([-*+]|\\d+\\.)\\s+(.*)$/);
        if(lm){{
          rawListBuf.push(lines[i]);
          i++;
        }} else if(/^\\s+\\S/.test(lines[i]) && rawListBuf.length > 0){{
          // 列表项的续行（缩进的非空行）
          rawListBuf.push(lines[i]);
          i++;
        }} else if(lines[i].trim() === '' && i+1 < n && /^(\\s*)([-*+]|\\d+\\.)\\s+/.test(lines[i+1])){{
          // 列表间的空行
          i++;
        }} else {{
          break;
        }}
      }}
      html.push(renderListBlock(rawListBuf));
      continue;
    }}

    // --- 空行 ---
    if(line.trim() === ''){{
      i++;
      continue;
    }}

    // --- 段落（连续非空行，直到遇到块级元素）---
    var paraBuf = [];
    while(i < n){{
      var l = lines[i];
      if(l.trim() === '') break;
      if(/^```/.test(l)) break;
      if(/^#{{1,6}}\\s/.test(l)) break;
      if(/^\\s*>/.test(l)) break;
      if(/^\\s*\\|/.test(l) && i+1 < n && /^\\s*\\|?\\s*:?-{{2,}}/.test(lines[i+1])) break;
      if(/^\\s*([-*_])\\1{{2,}}\\s*$/.test(l)) break;
      if(/^(\\s*)([-*+]|\\d+\\.)\\s+/.test(l)) break;
      paraBuf.push(l);
      i++;
    }}
    if(paraBuf.length){{
      html.push('<p>' + renderInlineSafe(paraBuf.join(' ')) + '</p>');
    }}
  }}

  return html.join('\\n');
}}

// 表格行拆分为单元格
function splitTableRow(row){{
  row = row.trim();
  if(row.charAt(0) === '|') row = row.slice(1);
  if(row.charAt(row.length-1) === '|') row = row.slice(0, -1);
  // 简单按 | 分割（不处理转义）
  return row.split('|').map(function(c){{ return c.trim(); }});
}}

// 渲染列表块（支持嵌套）
function renderListBlock(buf){{
  // 用缩进判断层级
  function indentOf(s){{ var m = s.match(/^(\\s*)/); return m ? m[1].replace(/\\t/g,'  ').length : 0; }}
  function parseItem(line){{
    var m = line.match(/^(\\s*)([-*+]|\\d+\\.)\\s+(.*)$/);
    return m ? {{ indent: m[1].replace(/\\t/g,'  ').length, marker: m[2], text: m[3] }} : null;
  }}

  var items = [];
  for(var k=0; k<buf.length; k++){{
    var pi = parseItem(buf[k]);
    if(pi){{
      items.push(pi);
    }} else {{
      // 续行并入上一个 item
      if(items.length) items[items.length-1].text += '\\n' + buf[k].replace(/^\\s+/, '');
    }}
  }}

  // 递归构建
  function build(items, startIdx, parentIndent){{
    var html = '';
    var ordered = /\\d+\\./.test(items[startIdx].marker);
    html += ordered ? '<ol>' : '<ul>';
    var idx = startIdx;
    while(idx < items.length && items[idx].indent >= parentIndent){{
      if(items[idx].indent > parentIndent){{
        // 嵌套子列表：找到同级或更高级为止
        var sub = build(items, idx, items[idx].indent);
        // 把子列表附在最近的 <li> 末尾
        html = html.replace(/<\\/li>$/, sub + '</li>');
        // 跳过子项
        var p = idx;
        while(p < items.length && items[p].indent >= items[idx].indent) p++;
        idx = p;
        continue;
      }}
      // 同级项
      var text = items[idx].text;
      // 段落内换行转 <br>（简单处理）
      html += '<li>' + renderInlineSafe(text.replace(/\\n/g,'<br>')) + '</li>';
      idx++;
    }}
    html += ordered ? '</ol>' : '</ul>';
    return {{ html: html, nextIdx: idx }};
  }}

  // 简化版：用最小缩进作为根
  var minIndent = Infinity;
  items.forEach(function(it){{ if(it.indent < minIndent) minIndent = it.indent; }});
  var result = build(items, 0, minIndent - 1);
  return result.html;
}}

/* ============================================================
   页面切换 / 路由
   ============================================================ */
var currentDocId = null;
var docPages = DOC_ORDER.map(function(d){{ return d[0]; }});  // ['01','02',...]

function showPage(pageKey){{
  // pageKey: 'home' 或 '01'..'07'
  document.querySelectorAll('.page').forEach(function(p){{ p.classList.remove('active'); }});
  document.querySelectorAll('.nav-item').forEach(function(a){{ a.classList.remove('active'); }});

  var navLink = document.querySelector('.nav-item[data-page="' + pageKey + '"]');
  if(navLink) navLink.classList.add('active');

  if(pageKey === 'home'){{
    document.getElementById('page-home').classList.add('active');
    document.getElementById('topbarTitle').textContent = 'Java / SQL 系统化学习教程';
    currentDocId = null;
  }} else {{
    document.getElementById('page-home').classList.remove('active');
    var meta = DOC_META[pageKey];
    if(meta){{
      document.getElementById('docStageTag').textContent = meta.stage;
      document.getElementById('docTitle').innerHTML = '<h1 style="border:none;padding:0;margin:0;">' + escapeHtml(meta.title) + '</h1>';
      var md = DOCS[pageKey];
      document.getElementById('docContent').innerHTML = renderMarkdown(md);
      document.getElementById('page-doc').classList.add('active');
      document.getElementById('topbarTitle').textContent = meta.title;
      currentDocId = pageKey;
    }} else {{
      document.getElementById('page-home').classList.add('active');
      currentDocId = null;
    }}
  }}
  window.scrollTo(0,0);
  updatePager();
  // 关闭移动端侧边栏
  closeSidebar();
}}

function updatePager(){{
  var prevBtn = document.getElementById('prevBtn');
  var nextBtn = document.getElementById('nextBtn');
  if(currentDocId === null){{
    prevBtn.disabled = true;
    nextBtn.disabled = docPages.length === 0;
    return;
  }}
  var idx = docPages.indexOf(currentDocId);
  prevBtn.disabled = (idx <= 0);
  nextBtn.disabled = (idx >= docPages.length - 1);
}}

function goPrev(){{
  if(currentDocId === null){{ location.hash = '#page-' + docPages[0]; return; }}
  var idx = docPages.indexOf(currentDocId);
  if(idx > 0) location.hash = '#page-' + docPages[idx-1];
}}
function goNext(){{
  if(currentDocId === null){{ location.hash = '#page-' + docPages[0]; return; }}
  var idx = docPages.indexOf(currentDocId);
  if(idx >= 0 && idx < docPages.length - 1) location.hash = '#page-' + docPages[idx+1];
}}

function routeFromHash(){{
  var h = location.hash.replace(/^#/, '');
  if(h.indexOf('page-') === 0){{
    showPage(h.slice(5));
  }} else {{
    showPage('home');
  }}
}}

/* ============================================================
   侧边栏移动端折叠
   ============================================================ */
function openSidebar(){{ document.getElementById('sidebar').classList.add('open'); document.getElementById('overlay').classList.add('show'); }}
function closeSidebar(){{ document.getElementById('sidebar').classList.remove('open'); document.getElementById('overlay').classList.remove('show'); }}

/* ============================================================
   返回顶部
   ============================================================ */
function setupBackToTop(){{
  var btn = document.getElementById('backToTop');
  window.addEventListener('scroll', function(){{ if(window.scrollY > 400) btn.classList.add('show'); else btn.classList.remove('show'); }});
  btn.addEventListener('click', function(){{ window.scrollTo({{top:0, behavior:'smooth'}}); }});
}}

/* ============================================================
   搜索（基于文档原文 + 标题，简单包含匹配）
   ============================================================ */
function setupSearch(){{
  var input = document.getElementById('searchInput');
  var results = document.getElementById('searchResults');
  var timer = null;
  // 预处理：构建可搜索索引
  var index = DOC_ORDER.map(function(d){{
    var id = d[0], title = d[2];
    var content = DOCS[id] || '';
    // 取每行第一句做片段
    var lines = content.split('\\n');
    return {{ id:id, title:title, content:content, lines:lines }};
  }});

  function search(q){{
    q = q.trim();
    if(!q){{ results.style.display='none'; return []; }}
    q = q.toLowerCase();
    var matches = [];
    index.forEach(function(doc){{
      var lower = doc.content.toLowerCase();
      var pos = lower.indexOf(q);
      if(pos >= 0){{
        var start = Math.max(0, pos - 30);
        var snippet = doc.content.substr(start, 80).replace(/\\n/g,' ');
        matches.push({{ doc:doc, snippet:snippet }});
      }}
    }});
    return matches;
  }}

  function renderResults(q){{
    var ms = search(q);
    if(ms.length === 0){{
      results.innerHTML = '<div class="sr-empty">未找到相关内容</div>';
      results.style.display='block';
      return;
    }}
    var html = '';
    ms.slice(0, 12).forEach(function(m){{
      html += '<a class="sr-item" href="#page-' + m.doc.id + '">'
        + '<div>' + escapeHtml(m.snippet) + '</div>'
        + '<div class="sr-doc">' + escapeHtml(m.doc.title) + '</div>'
        + '</a>';
    }});
    results.innerHTML = html;
    results.style.display='block';
  }}

  input.addEventListener('input', function(){{
    var v = input.value;
    clearTimeout(timer);
    if(!v.trim()){{ results.style.display='none'; return; }}
    timer = setTimeout(function(){{ renderResults(v); }}, 180);
  }});
  input.addEventListener('focus', function(){{ if(input.value.trim()) renderResults(input.value); }});
  document.addEventListener('click', function(e){{ if(!e.target.closest('.search-box')) results.style.display='none'; }});
}}

/* ============================================================
   初始化
   ============================================================ */
document.addEventListener('DOMContentLoaded', function(){{
  // 导航点击
  document.querySelectorAll('.nav-item').forEach(function(a){{
    a.addEventListener('click', function(){{ closeSidebar(); }});
  }});
  document.getElementById('menuToggle').addEventListener('click', function(){{ openSidebar(); }});
  document.getElementById('overlay').addEventListener('click', closeSidebar);
  document.getElementById('prevBtn').addEventListener('click', goPrev);
  document.getElementById('nextBtn').addEventListener('click', goNext);
  setupBackToTop();
  setupSearch();
  window.addEventListener('hashchange', routeFromHash);
  routeFromHash();
}});
</script>
</body>
</html>
"""
    return html


def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    docs = read_docs()
    html = build_html(docs)
    with open(OUT_FILE, "w", encoding="utf-8") as f:
        f.write(html)
    size = os.path.getsize(OUT_FILE)
    print(f"已生成: {OUT_FILE}")
    print(f"文件大小: {size} 字节 ({size/1024:.1f} KB)")


if __name__ == "__main__":
    main()
