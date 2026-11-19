// Local preview of the website with the same rules as Azure Static Web Apps:
// "/" serves index.html, "/about" serves about.html when that file exists, and
// every other address serves the fallback page named in staticwebapp.config.json.
//   node scripts/serve-web.mjs [port]
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
  '.xml': 'application/xml',
};

const fallback = join(root, config.navigationFallback.rewrite);

createServer(async (request, response) => {
  const path = normalize(decodeURIComponent(new URL(request.url, 'http://localhost').pathname));
  const headers = { ...config.globalHeaders };
  const send = (status, type, body) => {
    response.writeHead(status, { ...headers, 'Content-Type': type });
    response.end(body);
  };

  if (extname(path)) {
    try {
      send(200, types[extname(path)] || 'application/octet-stream', await readFile(join(root, path)));
    } catch {
      send(404, 'text/plain; charset=utf-8', 'Not found');
    }
    return;
  }

  // A page of its own (index.html, about.html, fruit/love.html), else the fallback.
  const page = path === '/' ? 'index.html' : `${path.replace(/\/$/, '')}.html`;
  const body = await readFile(join(root, page)).catch(() => readFile(fallback));
  send(200, types['.html'], body);
}).listen(port, () => console.log(`Scroll web preview: http://localhost:${port}`));
