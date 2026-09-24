// Deploy self-contained gzipped Worker to overwrite java-sql-ai-tutorial
// Worker code is embedded as base64 to avoid string escaping issues
const workerB64 = "__WORKER_CODE_B64__";
const workerCode = atob(workerB64);

const metadata = {
  main_module: "worker.js",
  compatibility_date: "2024-09-24",
  bindings: []
};

const boundary = "----WorkerDeploy" + Date.now();

async () => {
  // Build multipart/form-data body
  const metaPart = "--" + boundary + "\r\n" +
    'Content-Disposition: form-data; name="metadata"\r\n' +
    "Content-Type: application/json\r\n\r\n" +
    JSON.stringify(metadata) + "\r\n";
  const fileHeader = "--" + boundary + "\r\n" +
    'Content-Disposition: form-data; name="worker.js"; filename="worker.js"\r\n' +
    "Content-Type: application/javascript+module\r\n\r\n";
  const closing = "\r\n--" + boundary + "--\r\n";

  const enc = new TextEncoder();
  const headBytes = enc.encode(fileHeader);
  const metaBytes = enc.encode(metaPart);
  const tailBytes = enc.encode(closing);

  // Decode base64 worker code to bytes
  const workerBin = new Uint8Array(workerCode.length);
  for (let i = 0; i < workerCode.length; i++) workerBin[i] = workerCode.charCodeAt(i);

  const total = metaBytes.length + headBytes.length + workerBin.length + tailBytes.length;
  const body = new Uint8Array(total);
  let off = 0;
  body.set(metaBytes, off); off += metaBytes.length;
  body.set(headBytes, off); off += headBytes.length;
  body.set(workerBin, off); off += workerBin.length;
  body.set(tailBytes, off); off += tailBytes.length;

  // Use fetch() to upload - this inherits auth from MCP env for api.cloudflare.com
  const r = await fetch("https://api.cloudflare.com/client/v4/accounts/" + accountId + "/workers/scripts/java-sql-ai-tutorial", {
    method: "PUT",
    headers: { "Content-Type": "multipart/form-data; boundary=" + boundary },
    body: body
  });
  const text = await r.text();
  let parsed;
  try { parsed = JSON.parse(text); } catch (e) { parsed = { raw: text.substring(0, 500) }; }
  return {
    status: r.status,
    ok: r.ok,
    success: parsed.success,
    errors: parsed.errors,
    result_id: parsed.result?.id,
    result_size: parsed.result?.size,
    modified_on: parsed.result?.modified_on
  };
}
