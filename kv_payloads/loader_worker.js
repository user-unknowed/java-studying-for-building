// Loader worker: serves site content from KV.
// Each KV value is gzip+base64 encoded. Worker decodes and decompresses on demand.
// KV keys are paths without leading slash (e.g., "08.html", "assets/style.css").
const ROUTES = [{"path": "/01.html", "ct": "text/html; charset=utf-8"}, {"path": "/02.html", "ct": "text/html; charset=utf-8"}, {"path": "/03.html", "ct": "text/html; charset=utf-8"}, {"path": "/04.html", "ct": "text/html; charset=utf-8"}, {"path": "/05.html", "ct": "text/html; charset=utf-8"}, {"path": "/06.html", "ct": "text/html; charset=utf-8"}, {"path": "/07.html", "ct": "text/html; charset=utf-8"}, {"path": "/08.html", "ct": "text/html; charset=utf-8"}, {"path": "/assets/style.css", "ct": "text/css; charset=utf-8"}, {"path": "/index.html", "ct": "text/html; charset=utf-8"}];

const KV_NAMESPACE = "SITE_CONTENT";

export default {
  async fetch(request, env) {
    const u = new URL(request.url);
    let p = u.pathname;
    if (p === '/' || p === '') p = '/index.html';

    // Find content type for this path
    const route = ROUTES.find(r => r.path === p);
    if (!route) {
      // Try path + /index.html
      const alt = ROUTES.find(r => r.path === p.replace(/\/$/, '') + '/index.html');
      if (alt) {
        const key = alt.path.replace(/^\//, '');
        const b64 = await env[KV_NAMESPACE].get(key);
        if (b64) return await serveContent(b64, alt.ct);
      }
      return new Response('Not Found: ' + p, { status: 404, headers: { 'Content-Type': 'text/plain; charset=utf-8' } });
    }

    // KV key = path without leading slash
    const key = p.replace(/^\//, '');
    const b64 = await env[KV_NAMESPACE].get(key);
    if (!b64) {
      return new Response('KV miss: ' + key, { status: 404, headers: { 'Content-Type': 'text/plain; charset=utf-8' } });
    }

    return await serveContent(b64, route.ct);
  }
};

async function serveContent(b64, contentType) {
  // Decode base64 -> gzipped bytes -> decompress
  const gzStr = atob(b64);
  const gzBytes = new Uint8Array(gzStr.length);
  for (let i = 0; i < gzStr.length; i++) gzBytes[i] = gzStr.charCodeAt(i);
  const ds = new DecompressionStream('gzip');
  const blob = new Blob([gzBytes]).stream().pipeThrough(ds);
  const decompressed = await new Response(blob).arrayBuffer();
  return new Response(decompressed, {
    headers: {
      'Content-Type': contentType,
      'Cache-Control': 'public, max-age=3600',
      'Content-Encoding': 'identity',
      'Access-Control-Allow-Origin': '*'
    }
  });
}
