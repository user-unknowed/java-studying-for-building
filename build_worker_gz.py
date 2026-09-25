#!/usr/bin/env python3
"""Build a compact Worker with gzip-compressed site content embedded as base64.
The Worker decompresses on-demand using DecompressionStream (available in Workers runtime)."""
import base64
import gzip
import json
from pathlib import Path

SITE_DIR = Path("/workspace/site")
OUT_WORKER = Path("/workspace/worker_gz.js")

CONTENT_TYPES = {
    ".html": "text/html; charset=utf-8",
    ".css": "text/css; charset=utf-8",
    ".js": "application/javascript; charset=utf-8",
    ".json": "application/json; charset=utf-8",
    ".png": "image/png",
    ".svg": "image/svg+xml",
}


def collect_files(root: Path):
    files = []
    for p in root.rglob("*"):
        if p.is_file() and not p.name.startswith("."):
            rel = "/" + str(p.relative_to(root)).replace("\\", "/")
            files.append((rel, p))
    return sorted(files)


def main():
    files = collect_files(SITE_DIR)
    entries = []
    total_raw = 0
    total_gz = 0
    for url_path, file_path in files:
        raw = file_path.read_bytes()
        gz = gzip.compress(raw, compresslevel=9)
        b64 = base64.b64encode(gz).decode("ascii")
        ext = file_path.suffix.lower()
        ct = CONTENT_TYPES.get(ext, "application/octet-stream")
        entries.append({"path": url_path, "b64": b64, "ct": ct, "raw": len(raw), "gz": len(gz)})
        total_raw += len(raw)
        total_gz += len(gz)
        print(f"  {url_path:30s}  raw={len(raw):>7d}  gz={len(gz):>6d}  b64={len(b64):>6d}")

    routes_json = json.dumps(entries, ensure_ascii=False)

    worker_code = """// Self-contained Worker serving gzipped site content.
// Decompresses on-demand using DecompressionStream, then caches at edge.
const ROUTES = """ + routes_json + """;

export default {
  async fetch(request, env, ctx) {
    const u = new URL(request.url);
    let p = u.pathname;
    if (p === '/' || p === '') p = '/index.html';
    let entry = ROUTES.find(r => r.path === p);
    if (!entry && !p.endsWith('/')) {
      entry = ROUTES.find(r => r.path === p + '/index.html');
    }
    if (!entry) {
      return new Response('Not Found: ' + p, { status: 404, headers: { 'Content-Type': 'text/plain; charset=utf-8' } });
    }
    // Try edge cache first
    const cache = caches.default;
    let cached = await cache.match(request);
    if (cached) return cached;
    // Decode base64 -> gzipped bytes -> decompress
    const gzStr = atob(entry.b64);
    const gzBytes = new Uint8Array(gzStr.length);
    for (let i = 0; i < gzStr.length; i++) gzBytes[i] = gzStr.charCodeAt(i);
    const ds = new DecompressionStream('gzip');
    const blob = new Blob([gzBytes]).stream().pipeThrough(ds);
    const decompressed = await new Response(blob).arrayBuffer();
    const response = new Response(decompressed, {
      headers: {
        'Content-Type': entry.ct,
        'Cache-Control': 'public, max-age=86400',
        'CDN-Cache-Control': 'public, max-age=604800',
        'Access-Control-Allow-Origin': '*'
      }
    });
    // Cache at edge for subsequent requests (served from nearest PoP incl. China)
    ctx.waitUntil(cache.put(request, response.clone()));
    return response;
  }
};
"""

    OUT_WORKER.write_text(worker_code, encoding="utf-8")
    size = OUT_WORKER.stat().st_size
    print(f"\nWorker script written: {OUT_WORKER} ({size} bytes, {size/1024:.1f} KB)")
    print(f"Total raw: {total_raw} bytes ({total_raw/1024:.1f} KB)")
    print(f"Total gzipped: {total_gz} bytes ({total_gz/1024:.1f} KB)")
    print(f"Compression ratio: {total_gz/total_raw*100:.1f}%")


if __name__ == "__main__":
    main()
