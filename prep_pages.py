#!/usr/bin/env python3
"""预计算 Pages 部署所需的 assets（sha256 + base64 + manifest）"""
import os, hashlib, base64, json, mimetypes

SITE = "/workspace/site"
OUT = "/workspace/pages-deploy"
os.makedirs(OUT, exist_ok=True)
mimetypes.add_type("text/html", ".html")
mimetypes.add_type("text/css", ".css")

manifest = {}        # path -> sha256
assets = {}          # sha256 -> {value: base64, contentType}
file_list = []

for root, dirs, files in os.walk(SITE):
    for fn in files:
        fpath = os.path.join(root, fn)
        rel = os.path.relpath(fpath, SITE)
        # Pages 路径用正斜杠，以 / 开头
        logical = "/" + rel.replace(os.sep, "/")
        with open(fpath, "rb") as f:
            data = f.read()
        h = hashlib.sha256(data).hexdigest()
        ct = mimetypes.guess_type(fpath)[0] or "application/octet-stream"
        manifest[logical] = h
        if h not in assets:
            assets[h] = {"base64": True, "key": h,
                         "metadata": {"contentType": ct},
                         "value": base64.b64encode(data).decode("ascii")}
        file_list.append((logical, h, len(data), ct))

print(f"Total files: {len(file_list)}")
print(f"Unique assets: {len(assets)}")
total_bytes = sum(s for _,_,s,_ in file_list)
print(f"Total bytes: {total_bytes}")

# 写 manifest
with open(f"{OUT}/manifest.json", "w", encoding="utf-8") as f:
    json.dump(manifest, f)

# 分批 assets（每批尽量不超过 ~250KB base64）
batches = []
cur = []
cur_size = 0
LIMIT = 250 * 1024  # 250KB base64 per batch
asset_list = list(assets.values())
# 按大小降序，便于分批
asset_list.sort(key=lambda a: len(a["value"]), reverse=True)
for a in asset_list:
    sz = len(a["value"])
    if cur and cur_size + sz > LIMIT:
        batches.append(cur)
        cur = []
        cur_size = 0
    cur.append(a)
    cur_size += sz
if cur:
    batches.append(cur)

print(f"Batches: {len(batches)}")
for i, b in enumerate(batches):
    path = f"{OUT}/batch_{i:02d}.json"
    with open(path, "w", encoding="utf-8") as f:
        json.dump(b, f)
    total_b64 = sum(len(x["value"]) for x in b)
    print(f"  batch_{i:02d}: {len(b)} files, {total_b64} bytes b64")

# 打印文件清单
print("\nFile list:")
for logical, h, sz, ct in file_list:
    print(f"  {logical}  {sz}B  {ct}")
