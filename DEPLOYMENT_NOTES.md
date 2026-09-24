# Deployment Notes — 2026-09-25 (updated)

## Live URL (temporary preview account — Misty Chrysanthemum)
- Worker URL: https://java-sql-ai-tutorial-temp.misty-chrysanthemum.workers.dev
- Python 现代化语法 chapter (08.html): https://java-sql-ai-tutorial-temp.misty-chrysanthemum.workers.dev/08.html
- All 10 site files served from the self-contained worker_gz.js (gzip+base64 inline)

## Claim the temp account (within 60 minutes)
https://dash.cloudflare.com/claim-preview?claimToken=3NNxDWdRmiUq29j08QClr-qhD5Zsd9KeedzQxN_Hw1Y

## Worker / Version
- Worker name: java-sql-ai-tutorial-temp
- Temp account name: Misty Chrysanthemum
- Account ID: 9cd33d7a05223ebd94c012ac9d4897cc
- Version ID: 16ffb3aa-1f5c-4805-95b3-45253a3812ff
- Worker source: /workspace/worker_gz.js (217,917 bytes)
- Wrangler config: /workspace/wrangler.toml

## Verification Results (2026-09-25)

### Cloudflare API verification (account-scoped)
- ✅ Worker script deployed (GET /scripts returned 200, 218 KB)
- ✅ All 10 routes present: /, /01-08.html, /assets/style.css
- ✅ Subdomain enabled: misty-chrysanthemum.workers.dev
- ✅ Settings: compatibility_date=2024-09-23, usage_model=standard

### Local workerd runtime tests (wrangler dev)
| Path | Status | Content-Type | Size | Title |
|------|--------|-------------|------|-------|
| / | 200 | text/html | 6,882 | Java · SQL · Python AI 系统化教程 |
| /01.html | 200 | text/html | 312,634 | 01 JavaSE 基础知识点 |
| /02.html | 200 | text/html | 20,115 | 02 Java 与 SQL 数据库深度融合 |
| /03.html | 200 | text/html | 45,661 | 03 SQL 数据库深度讲解 |
| /04.html | 200 | text/html | 14,632 | 04 学习报告 |
| /05.html | 200 | text/html | 116,415 | 05 JavaEE 企业级开发 |
| /06.html | 200 | text/html | 63,107 | 06 Java 上位机开发实战 |
| /07.html | 200 | text/html | 43,481 | 07 JavaEE 上位机：网页监控看板 |
| /08.html | 200 | text/html | 78,471 | 08 Python 与 AI 大模型：从原理到 Transformer |
| /assets/style.css | 200 | text/css | 11,232 | — |
| /nonexistent | 404 | text/plain | 23 | — |

### 08.html Python modern syntax keyword check
- Protocol: 9, async: 37, dataclass: 16, f-string: 6, match/case: 6, typing: 8, walrus: 3

### Network limitation
- Sandbox cannot reach *.workers.dev (TLS blocked at network level)
- api.cloudflare.com, dash.cloudflare.com are reachable
- Live URL verified by user from their own browser

## Contents shipped
- 01.html (312 KB raw / 55 KB gz)
- 02.html (20 KB raw / 7 KB gz)
- 03.html (46 KB raw / 14 KB gz)
- 04.html (15 KB raw / 6 KB gz)
- 05.html (116 KB raw / 30 KB gz)
- 06.html (63 KB raw / 16 KB gz)
- 07.html (43 KB raw / 10 KB gz)
- 08.html (78 KB raw / 18 KB gz) — NEW: includes modern Python syntax chapter
- index.html (6.9 KB raw / 2.7 KB gz)
- assets/style.css (11 KB raw / 3.2 KB gz)

## How to update the original java-sql-ai-tutorial worker
The original worker (account 2450b16d188a6686db33d4e1b31aff1a, KV namespace
0f043601631040b49a4539fc9809885f) requires authentication to update.

### Option A — Cloudflare API token (recommended)
1. Visit https://dash.cloudflare.com/profile/api-tokens
2. Create a token with permissions: Account → Workers Scripts → Edit, Account → Workers KV Storage → Edit
3. Set environment variables and deploy with wrangler:
   ```
   export CLOUDFLARE_API_TOKEN=...
   export CLOUDFLARE_ACCOUNT_ID=2450b16d188a6686db33d4e1b31aff1a
   cd /workspace
   ./node_modules/.bin/wrangler deploy worker_gz.js --name java-sql-ai-tutorial --compatibility-date 2024-09-23
   ```

### Option B — Browser login then wrangler login
1. In the Trae browser tab, log into Cloudflare dash
2. After returning to chat, run: `./node_modules/.bin/wrangler login`
3. After OAuth completes, run the deploy command above (omit CLOUDFLARE_API_TOKEN)

## Files available for re-deployment
- /workspace/worker_gz.js — self-contained worker (uses inlined gzip+base64 content)
- /workspace/kv_payloads/loader_worker.js — KV-backed worker (requires KV binding SITE_CONTENT)
- /workspace/site/ — source files (8 chapters + index + assets/style.css)
- /workspace/build_worker_gz.py — regenerates worker_gz.js from /workspace/site
