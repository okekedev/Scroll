// Local preview of the website with the same "every address serves index.html"
// behaviour as Azure Static Web Apps.   node scripts/serve-web.mjs [port]
import { createServer } from 'node:http';
import { readFile } from 'node:fs/promises';
import { extname, join, normalize } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = join(fileURLToPath(new URL('.', import.meta.url)), '..', 'web');
const port = Number(process.argv[2]) || 4173;
const config = JSON.parse(await readFile(join(root, 'staticwebapp.config.json'), 'utf8'));
const types = {
  '.html': 'text/html; charset=utf-8', '.css': 'text/css; charset=utf-8', '.js': 'text/javascript; charset=utf-8',
  '.mjs': 'text/javascript; charset=utf-8', '.json': 'application/json', '.webmanifest': 'application/manifest+json',
  '.png': 'image/png', '.jpg': 'image/jpeg', '.woff2': 'font/woff2', '.txt': 'text/plain; charset=utf-8',
};

createServer(async (request, response) => {
  const path = normalize(decodeURIComponent(new URL(request.url, 'http://localhost').pathname));
  const headers = { ...config.globalHeaders };
  try {
    if (!extname(path)) throw new Error('route');
    const body = await readFile(join(root, path));
    response.writeHead(200, { ...headers, 'Content-Type': types[extname(path)] || 'application/octet-stream' });
    response.end(body);
  } catch {
    if (extname(path)) { response.writeHead(404, headers); response.end('Not found'); return; }
    response.writeHead(200, { ...headers, 'Content-Type': types['.html'] });
    response.end(await readFile(join(root, 'index.html')));
  }
}).listen(port, () => console.log(`Scroll web preview: http://localhost:${port}`));
