#!/usr/bin/env python3
"""Prepare site files for KV upload: gzip + base64 encode each file."""
import gzip, base64, json, mimetypes, os

SITE_DIR = "/workspace/site"
KV_FILE = "/workspace/kv_data.json"

entries = []
for root, _, files in os.walk(SITE_DIR):
    for fn in sorted(files):
        full = os.path.join(root, fn)
        rel = os.path.relpath(full, SITE_DIR).replace(os.sep, "/")
        key = "/" + rel
        raw = open(full, "rb").read()
        gz = gzip.compress(raw)
        b64 = base64.b64encode(gz).decode("ascii")
        ct, _ = mimetypes.guess_type(full)
        ct = ct or "application/octet-stream"
        entries.append({
            "key": key,
            "b64": b64,
            "ct": ct,
            "raw_size": len(raw),
            "gz_size": len(gz),
            "b64_size": len(b64),
        })

with open(KV_FILE, "w") as f:
    json.dump(entries, f)

print(f"Total files: {len(entries)}")
for e in entries:
    print(f"  {e['key']:<25} raw={e['raw_size']:>7} gz={e['gz_size']:>6} b64={e['b64_size']:>6}  ct={e['ct']}")

total_b64 = sum(e['b64_size'] for e in entries)
max_b64 = max(e['b64_size'] for e in entries)
print(f"\nTotal base64: {total_b64} bytes ({total_b64/1024:.1f} KB)")
print(f"Max single file b64: {max_b64} bytes ({max_b64/1024:.1f} KB)")
