// Worker serving site from KV namespace
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
