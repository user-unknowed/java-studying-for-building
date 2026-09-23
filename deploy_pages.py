#!/usr/bin/env python3
"""
一键部署教程网站到 Cloudflare Pages（Direct Upload）。

用法：
    export CLOUDFLARE_API_TOKEN=你的_API_Token
    export CLOUDFLARE_ACCOUNT_ID=2450b16d188a6686db33d4e1b31aff1a
    python3 deploy_pages.py

依赖：requests  (pip install requests)

注意：
- manifest 的键必须带前导斜杠（/index.html, /01.html, /assets/style.css）
  这是 wrangler 源码中 urlSafe(fileName) 后再拼 / 的实际格式。
- 值为文件内容的 SHA-256 哈希（十六进制）。
"""
import os, sys, json, hashlib, base64, mimetypes
import requests

ACCOUNT_ID = os.environ.get("CLOUDFLARE_ACCOUNT_ID", "2450b16d188a6686db33d4e1b31aff1a")
PROJECT = "java-sql-ai-tutorial"
SITE_DIR = "/workspace/site"
API = "https://api.cloudflare.com/client/v4"

def api_token():
    t = os.environ.get("CLOUDFLARE_API_TOKEN")
    if not t:
        sys.exit("请设置环境变量 CLOUDFLARE_API_TOKEN")
    return t

def sha256_file(path):
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(1 << 20), b""):
            h.update(chunk)
    return h.hexdigest()

def main():
    token = api_token()
    auth = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}

    # 1. 收集文件 → (logical_path, hash, base64_content, content_type)
    assets = []
    manifest = {}
    for root, _, files in os.walk(SITE_DIR):
        for fn in files:
            full = os.path.join(root, fn)
            rel = os.path.relpath(full, SITE_DIR).replace(os.sep, "/")
            logical = "/" + rel  # ← 关键：前导斜杠
            h = sha256_file(full)
            with open(full, "rb") as f:
                b64 = base64.b64encode(f.read()).decode()
            ct, _ = mimetypes.guess_type(full)
            assets.append({"key": h, "value": b64, "base64": True,
                           "contentType": ct or "application/octet-stream"})
            manifest[logical] = h
            print(f"  {logical}  {h[:12]}…  {len(b64)} b64")

    # 2. 获取 upload token（JWT）
    r = requests.get(f"{API}/accounts/{ACCOUNT_ID}/pages/projects/{PROJECT}/upload-token",
                     headers={"Authorization": f"Bearer {token}"})
    r.raise_for_status()
    jwt = r.json()["result"]["jwt"]
    print(f"\nupload-token ok (exp in jwt)")

    # 3. 上传资产（分批，每批 <= ~250KB base64）
    batch, batch_size = [], 0
    MAX = 240 * 1024
    batches = []
    for a in assets:
        sz = len(a["value"])
        if batch and batch_size + sz > MAX:
            batches.append(batch); batch, batch_size = [], 0
        batch.append(a); batch_size += sz
    if batch:
        batches.append(batch)

    for i, b in enumerate(batches):
        r = requests.post("https://api.cloudflare.com/client/v4/pages/assets/upload",
                          headers={"Authorization": f"Bearer {jwt}",
                                   "Content-Type": "application/json"},
                          data=json.dumps(b))
        r.raise_for_status()
        print(f"  upload batch {i+1}/{len(batches)}: {r.json().get('success')}")

    # 4. 创建 deployment（multipart/form-data，manifest 字段为 JSON 字符串）
    boundary = "----PagesDeploy" + os.urandom(8).hex()
    body = (
        f"--{boundary}\r\n"
        f'Content-Disposition: form-data; name="manifest"\r\n'
        f"Content-Type: application/json\r\n\r\n"
        f"{json.dumps(manifest)}\r\n"
        f"--{boundary}--\r\n"
    )
    r = requests.post(
        f"{API}/accounts/{ACCOUNT_ID}/pages/projects/{PROJECT}/deployments",
        headers={"Authorization": f"Bearer {token}",
                 "Content-Type": f"multipart/form-data; boundary={boundary}"},
        data=body,
    )
    r.raise_for_status()
    res = r.json()
    dep = res.get("result", {})
    print(f"\n✅ deployment created: {dep.get('url')}")
    print(f"   id: {dep.get('id')}")
    print(f"   production domain: https://{PROJECT}.pages.dev")

if __name__ == "__main__":
    main()
