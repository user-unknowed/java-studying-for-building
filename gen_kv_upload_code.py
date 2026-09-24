#!/usr/bin/env python3
"""Generate individual MCP execute code files for uploading each KV payload.
Each generated file contains a complete JS async arrow function that uploads
one site file's base64 content to KV."""
import json
from pathlib import Path

PAYLOAD_DIR = Path("/workspace/kv_payloads")
OUT_DIR = Path("/workspace/kv_upload_code")
OUT_DIR.mkdir(exist_ok=True)

KV_NS = "0f043601631040b49a4539fc9809885f"
ACCOUNT_ID = "2450b16d188a6686db33d4e1b31aff1a"

# Files already uploaded
ALREADY_UPLOADED = {"index.html"}

def main():
    manifest_path = PAYLOAD_DIR / "manifest.json"
    with open(manifest_path) as f:
        manifest = json.load(f)

    # Skip already uploaded files
    to_upload = [m for m in manifest if m["path"].strip("/") not in ALREADY_UPLOADED]

    # Also skip 04.html and assets/style.css and index.html (already done)
    done = {"index.html", "04.html", "assets/style.css"}
    to_upload = [m for m in to_upload if m["path"].strip("/") not in done]

    print(f"Files to generate upload code for: {len(to_upload)}")

    for m in to_upload:
        path = m["path"]  # e.g., "/08.html"
        key = path.strip("/")  # e.g., "08.html"
        # URL-encode the key for the API path
        url_key = key.replace("/", "%2F")

        # Read the payload file to get the b64 content
        payload_file = Path(m["file"])
        with open(payload_file) as f:
            payload = json.load(f)
        b64 = payload["b64"]

        # Generate JS code
        js_code = f"""async () => {{
  const ns = '{KV_NS}';
  const b64 = '{b64}';
  const r = await cloudflare.request({{
    method: 'PUT',
    path: `/accounts/${{accountId}}/storage/kv/namespaces/${{ns}}/values/{url_key}`,
    body: b64,
    contentType: 'text/plain; charset=utf-8',
    rawBody: true
  }});
  return {{ f: '{key}', ok: r.success, e: r.errors }};
}}"""

        # Write to file
        safe_name = key.replace("/", "_")
        out_file = OUT_DIR / f"upload_{safe_name}.js"
        with open(out_file, "w") as f:
            f.write(js_code)

        print(f"  {key}: b64={len(b64)}B -> {out_file.name} ({len(js_code)}B)")

if __name__ == "__main__":
    main()
