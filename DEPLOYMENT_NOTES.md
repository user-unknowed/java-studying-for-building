# Deployment Notes — 2026-09-24

## Live URL (temporary preview account)
- Worker URL: https://java-sql-ai-tutorial-temp.aboard-cake.workers.dev
- Python 现代化语法 chapter (08.html): https://java-sql-ai-tutorial-temp.aboard-cake.workers.dev/08.html
- All 10 site files served from the self-contained worker_gz.js (gzip+base64 inline)

## Claim the temp account (within 60 minutes)
https://dash.cloudflare.com/claim-preview?claimToken=Pjamr3TBOJYLdVeTg_OBJDmBihMrJvWblbAlq5MpdQI

## Worker / Version
- Worker name: java-sql-ai-tutorial-temp
- Temp account name: Aboard Cake
- Version ID: 80d93122-b421-494d-b273-9c199387d9c6
- Worker source: /workspace/worker_gz.js (217,917 bytes)
- Wrangler config: /workspace/wrangler.toml

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
