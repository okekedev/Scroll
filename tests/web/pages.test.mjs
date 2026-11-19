// Run with: node --test tests/web/*.test.mjs
// Checks the static pages search engines read (written by scripts/build-pages.mjs).
import test from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { join } from 'node:path';

import { buildFiles, staleFiles, WEB_ROOT, INDEXNOW_KEY } from '../../scripts/build-pages.mjs';
import { pageMeta, sitemapRoutes } from '../../web/js/meta.js';
import { FRUITS } from '../../web/js/bible.js';

const files = buildFiles();
const ORIGIN = 'https://scroll.okeke.us';

/** The static file the host serves for a path: "/" → index.html, "/about" → about.html. */
function fileFor(path) {
  return path === '/' ? 'index.html' : `${path.slice(1)}.html`;
}

function tag(html, pattern) {
  const match = pattern.exec(html);
  return match ? match[1] : null;
}

test('the static pages on disk are up to date', async () => {
  assert.deepEqual(await staleFiles(), [], 'run: node scripts/build-pages.mjs');
});

test('every sitemap page has its own file, title, description, canonical address and one h1', () => {
  const titles = new Set();
  const descriptions = new Set();
  for (const route of sitemapRoutes()) {
    const meta = pageMeta(route);
    const html = files.get(fileFor(meta.path));
    assert.ok(html, `no static file for ${meta.path}`);

    const title = tag(html, /<title>([^<]*)<\/title>/);
    const description = tag(html, /<meta name="description" content="([^"]*)">/);
    assert.ok(title.length >= 10 && title.length <= 60, `title length for ${meta.path}: ${title.length}`);
    assert.ok(description.length >= 70 && description.length <= 160, `description length for ${meta.path}: ${description.length}`);
    assert.ok(!titles.has(title), `duplicate title: ${title}`);
    assert.ok(!descriptions.has(description), `duplicate description on ${meta.path}`);
    titles.add(title);
    descriptions.add(description);

    assert.equal(tag(html, /<link rel="canonical" href="([^"]*)">/), ORIGIN + meta.path);
    assert.equal(tag(html, /<meta property="og:url" content="([^"]*)">/), ORIGIN + meta.path);
    assert.equal((html.match(/<h1[ >]/g) || []).length, 1, `h1 count on ${meta.path}`);
    assert.ok(!html.includes('name="robots"'), `${meta.path} must be indexable`);
  }
  assert.equal(titles.size, FRUITS.length + 3);
});

test('the sitemap lists exactly those pages', () => {
  const listed = [...files.get('sitemap.xml').matchAll(/<loc>([^<]+)<\/loc>/g)].map((match) => match[1]);
  assert.deepEqual(listed, sitemapRoutes().map((route) => ORIGIN + pageMeta(route).path));
  assert.ok(files.get('robots.txt').includes(`Sitemap: ${ORIGIN}/sitemap.xml`));
});

test('links between the static pages all lead to a real page', () => {
  const known = new Set(sitemapRoutes().map((route) => pageMeta(route).path));
  for (const [name, html] of files) {
    if (!name.endsWith('.html')) continue;
    for (const [, href] of html.matchAll(/<a [^>]*href="(\/[^"]*)"/g)) {
      const isChapter = /^\/[A-Z]+\/\d+\/\d+\/(\d+(-\d+)?)?$/.test(href);
      assert.ok(known.has(href) || isChapter, `${name} links to ${href}`);
    }
  }
});

test('search, saved and the fallback page carry no canonical address', () => {
  for (const name of ['search.html', 'saved.html', 'shell.html']) {
    assert.ok(!files.get(name).includes('rel="canonical"'), name);
  }
  assert.ok(files.get('search.html').includes('<meta name="robots" content="noindex">'));
  assert.ok(files.get('saved.html').includes('<meta name="robots" content="noindex">'));
  // The fallback serves chapters too, so it must stay indexable.
  assert.ok(!files.get('shell.html').includes('name="robots"'));
});

test('a verse address is canonical to its chapter', () => {
  const meta = pageMeta({ name: 'reader', translation: 'ESV', book: 43, chapter: 3, verses: [16] });
  assert.equal(meta.path, '/ESV/43/3/');
  assert.equal(meta.title, 'John 3 ESV | Scroll');
  assert.equal(meta.index, true);
});

test('pages that are not content are kept out of search results', () => {
  for (const route of [{ name: 'search', query: 'love' }, { name: 'saved' }, { name: 'notfound' }]) {
    const meta = pageMeta(route);
    assert.equal(meta.index, false, route.name);
    assert.equal(meta.path, null, route.name);
  }
});

test('the host falls back to the generic page and never to the home page', async () => {
  const config = JSON.parse(await readFile(join(WEB_ROOT, 'staticwebapp.config.json'), 'utf8'));
  assert.equal(config.navigationFallback.rewrite, '/shell.html');
  assert.ok(config.navigationFallback.exclude.includes('/*.xml'));
});

test('llms.txt lists the same pages as the sitemap', () => {
  const llms = files.get('llms.txt');
  for (const route of sitemapRoutes()) {
    const url = ORIGIN + pageMeta(route).path;
    assert.ok(llms.includes(`(${url})`), `llms.txt is missing ${url}`);
  }
});

test('the IndexNow key file holds its own name', () => {
  assert.match(INDEXNOW_KEY, /^[0-9a-f]{32}$/);
  assert.equal(files.get(`${INDEXNOW_KEY}.txt`).trim(), INDEXNOW_KEY);
});
