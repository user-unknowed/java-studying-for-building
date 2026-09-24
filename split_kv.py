#!/usr/bin/env python3
"""Split KV data into individual files for upload."""
import json, os

KV_FILE = "/workspace/kv_data.json"
KV_DIR = "/workspace/kv_chunks"
os.makedirs(KV_DIR, exist_ok=True)

with open(KV_FILE) as f:
    entries = json.load(f)

manifest = []
for i, e in enumerate(entries):
    chunk_file = os.path.join(KV_DIR, f"chunk_{i:02d}_{e['key'].replace('/', '_').strip('_')}.json")
    chunk = {"key": e["key"], "b64": e["b64"], "ct": e["ct"]}
    with open(chunk_file, "w") as f:
        json.dump(chunk, f)
    manifest.append({
        "idx": i,
        "chunk_file": chunk_file,
        "key": e["key"],
        "b64_size": e["b64_size"],
        "chunk_file_size": os.path.getsize(chunk_file),
    })

print("Individual chunk files:")
for m in manifest:
    fits = "YES" if m["chunk_file_size"] < 60000 else "NO"
    print(f"  [{m['idx']}] {m['key']:<25} b64={m['b64_size']:>6} file={m['chunk_file_size']:>6} fits_in_64KB={fits}")

# Also write the small Worker script that reads from KV
worker = """// Worker serving site from KV namespace
const NS_ID = "793df3c5415445b98fa28ff6a1697c2b";
const CONTENT_TYPES = {"text/html": "text/html; charset=utf-8", "text/css": "text/css; charset=utf-8", "application/octet-stream": "application/octet-stream"};

export default {
  async fetch(request, env) {
    const u = new URL(request.url);
    let p = u.pathname;
    if (p === '/' || p === '') p = '/index.html';

    // Try exact path first
    let key = p;
    let meta = await env.SITE_CONTENT.getWithMetadata(key);
    let entry = meta?.value;
    let contentType = meta?.metadata?.ct;

    if (!entry && !p.endsWith('/')) {
      const k2 = p + '/index.html';
      meta = await env.SITE_CONTENT.getWithMetadata(k2);
      entry = meta?.value;
      contentType = meta?.metadata?.ct;
    }

    if (!entry) {
      return new Response('Not Found: ' + p, { status: 404, headers: { 'Content-Type': 'text/plain; charset=utf-8' } });
    }

    // entry is base64-encoded gzip data
    const gzStr = atob(entry);
    const gzBytes = new Uint8Array(gzStr.length);
    for (let i = 0; i < gzStr.length; i++) gzBytes[i] = gzStr.charCodeAt(i);
    const ds = new DecompressionStream('gzip');
    const blob = new Blob([gzBytes]).stream().pipeThrough(ds);
    const decompressed = await new Response(blob).arrayBuffer();

    return new Response(decompressed, {
      headers: {
        'Content-Type': contentType || 'application/octet-stream',
        'Cache-Control': 'public, max-age=3600',
        'Access-Control-Allow-Origin': '*'
      }
    });
  }
};
"""
with open("/workspace/worker_kv.js", "w") as f:
    f.write(worker)
print(f"\nWorker script written: /workspace/worker_kv.js ({len(worker)} bytes)")
