#!/usr/bin/env python3
"""Generate the JS code for Cloudflare execute tool to deploy worker_gz.js.
Uses template literal (backticks) to embed the worker script directly,
since the worker has no backticks or ${} sequences."""

with open('/workspace/worker_gz.js', 'r') as f:
    worker_content = f.read()

# Verify no problematic chars
assert '`' not in worker_content, "Worker contains backticks!"
assert '${' not in worker_content, "Worker contains ${}!"

# JS code using template literal for the worker content
js_code = 'async () => {\n'
js_code += '  // Worker script embedded as template literal\n'
js_code += '  const workerScript = `' + worker_content + '`;\n'
js_code += '\n'
js_code += '  // Build multipart/form-data body for ES module worker upload\n'
js_code += '  const boundary = "----WorkerDeploy" + Date.now();\n'
js_code += '  const metadata = {\n'
js_code += '    main_module: "worker.js",\n'
js_code += '    compatibility_date: "2024-09-23",\n'
js_code += '    bindings: []\n'
js_code += '  };\n'
js_code += '\n'
js_code += '  const parts = [\n'
js_code += '    `--${boundary}`,\n'
js_code += '    \'Content-Disposition: form-data; name="metadata"\',\n'
js_code += '    \'Content-Type: application/json\',\n'
js_code += '    \'\',\n'
js_code += '    JSON.stringify(metadata),\n'
js_code += '    `--${boundary}`,\n'
js_code += '    \'Content-Disposition: form-data; name="worker.js"; filename="worker.js"\',\n'
js_code += '    \'Content-Type: application/javascript+module\',\n'
js_code += '    \'\',\n'
js_code += '    workerScript,\n'
js_code += '    `--${boundary}--`,\n'
js_code += '    \'\'\n'
js_code += '  ];\n'
js_code += '  const body = parts.join(\'\\r\n\');\n'
js_code += '\n'
js_code += '  const res = await cloudflare.request({\n'
js_code += '    method: "PUT",\n'
js_code += '    path: `/accounts/${accountId}/workers/scripts/java-sql-ai-tutorial`,\n'
js_code += '    body: body,\n'
js_code += '    contentType: `multipart/form-data; boundary=${boundary}`,\n'
js_code += '    rawBody: true\n'
js_code += '  });\n'
js_code += '\n'
js_code += '  return {\n'
js_code += '    success: res.success,\n'
js_code += '    status: res.status,\n'
js_code += '    result: res.result,\n'
js_code += '    errors: res.errors,\n'
js_code += '    messages: res.messages,\n'
js_code += '    workerSize: workerScript.length,\n'
js_code += '    bodySize: body.length\n'
js_code += '  };\n'
js_code += '}\n'

with open('/workspace/deploy_worker_code.js', 'w') as f:
    f.write(js_code)

print(f"Worker content size: {len(worker_content)} bytes")
print(f"JS code size: {len(js_code)} chars")
print(f"Saved to: /workspace/deploy_worker_code.js")

# Verify the code is valid by checking structure
with open('/workspace/deploy_worker_code.js') as f:
    c = f.read()
print(f"Starts with: {c[:50]!r}")
print(f"Ends with: {c[-50:]!r}")
print(f"Backtick count: {c.count('`')}")
