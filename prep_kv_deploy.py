#!/usr/bin/env python3
"""Prepare KV payloads (gzip+base64) for each site file and generate a loader worker.

Each payload file is small enough (<60KB) to embed in a single MCP execute call.
The loader worker reads from KV, decompresses, and serves content.
"""
import gzip
import base64
import json
import os
from pathlib import Path

SITE_ROOT = Path("/workspace/site")
OUT_DIR = Path("/workspace/kv_payloads")
OUT_DIR.mkdir(exist_ok=True)

CONTENT_TYPES = {
    ".html": "text/html; charset=utf-8",
    ".css": "text/css; charset=utf-8",
    ".js": "application/javascript; charset=utf-8",
    ".json": "application/json; charset=utf-8",
    ".png": "image/png",
    ".jpg": "image/jpeg",
    ".svg": "image/svg+xml",
}

def collect_files(root: Path):
    files = []
    for p in sorted(root.rglob("*")):
        if p.is_file() and not p.name.startswith("."):
            rel = "/" + str(p.relative_to(root)).replace("\\", "/")
            files.append((rel, p))
    return files

def make_payload(path: str, file_path: Path):
    raw = file_path.read_bytes()
    gz = gzip.compress(raw, compresslevel=9)
    b64 = base64.b64encode(gz).decode("ascii")
    ext = file_path.suffix.lower()
    ct = CONTENT_TYPES.get(ext, "application/octet-stream")
    return {"path": path, "ct": ct, "b64": b64}

def main():
    files = collect_files(SITE_ROOT)
    print(f"Found {len(files)} site files")

    manifest = []
    for path, fpath in files:
        payload = make_payload(path, fpath)
        # Write individual payload file
        safe_name = path.strip("/").replace("/", "_")
        out_file = OUT_DIR / f"{safe_name}.json"
        with open(out_file, "w") as f:
            json.dump(payload, f)
        manifest.append({
            "path": path,
            "ct": payload["ct"],
            "b64_len": len(payload["b64"]),
            "file": str(out_file)
        })
        print(f"  {path}: raw={fpath.stat().st_size}B, b64={len(payload['b64'])}B -> {out_file.name}")

    # Write manifest
    with open(OUT_DIR / "manifest.json", "w") as f:
        json.dump(manifest, f, indent=2)

    # Generate loader worker
    loader = generate_loader_worker(manifest)
    with open(OUT_DIR / "loader_worker.js", "w") as f:
        f.write(loader)
    print(f"\nLoader worker: {len(loader)}B -> loader_worker.js")
    print(f"Total payloads: {len(manifest)}")

def generate_loader_worker(manifest):
    # Build a route map (path -> content_type) that the worker uses
    routes = [{"path": m["path"], "ct": m["ct"]} for m in manifest]
    routes_json = json.dumps(routes, ensure_ascii=False)

    return f"""// Loader worker: serves site content from KV.
// Each KV value is gzip+base64 encoded. Worker decodes and decompresses on demand.
const ROUTES = {routes_json};

const KV_NAMESPACE = "SITE_CONTENT";

export default {{
  async fetch(request, env) {{
    const u = new URL(request.url);
    let p = u.pathname;
    if (p === '/' || p === '') p = '/index.html';

    // Find content type for this path
    const route = ROUTES.find(r => r.path === p);
    if (!route) {{
      // Try directory + index.html
      const alt = ROUTES.find(r => r.path === p + '/index.html' || r.path === p.replace(//$/, '') + '/index.html');
      if (alt) {{
        const b64 = await env[KV_NAMESPACE].get(alt.path);
        if (b64) return await serveContent(b64, alt.ct);
      }}
      return new Response('Not Found: ' + p, {{ status: 404, headers: {{ 'Content-Type': 'text/plain; charset=utf-8' }} }});
    }}

    const b64 = await env[KV_NAMESPACE].get(p);
    if (!b64) {{
      return new Response('KV miss: ' + p, {{ status: 404, headers: {{ 'Content-Type': 'text/plain; charset=utf-8' }} }});
    }}

    return await serveContent(b64, route.ct);
  }}
}};

async function serveContent(b64, contentType) {{
  // Decode base64 -> gzipped bytes -> decompress
  const gzStr = atob(b64);
  const gzBytes = new Uint8Array(gzStr.length);
  for (let i = 0; i < gzStr.length; i++) gzBytes[i] = gzStr.charCodeAt(i);
  const ds = new DecompressionStream('gzip');
  const blob = new Blob([gzBytes]).stream().pipeThrough(ds);
  const decompressed = await new Response(blob).arrayBuffer();
  return new Response(decompressed, {{
    headers: {{
      'Content-Type': contentType,
      'Cache-Control': 'public, max-age=3600',
      'Content-Encoding': 'identity',
      'Access-Control-Allow-Origin': '*'
    }}
  }});
}}
"""

if __name__ == "__main__":
    main()
