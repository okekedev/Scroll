// Writes the static pages, sitemap and search-engine files under web/.
//
// The site is a single-page app, but search engines and link previews read the
// HTML before any JavaScript runs. So every page worth finding gets a real HTML
// file with its own title, description, canonical address and finished content.
// app.js then takes over in the browser. The content comes from the same
// functions app.js uses (web/js/pages.js, web/js/meta.js), so the two cannot drift.
//
//   node scripts/build-pages.mjs           write the files
//   node scripts/build-pages.mjs --check   exit 1 if a file on disk is out of date
//
// Run it after changing pages.js, meta.js or bible.js and commit the result.

import { readFile, writeFile, mkdir } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';

import { FRUITS } from '../web/js/bible.js';
import { SITE_ORIGIN } from '../web/js/format.js';
import { pageMeta, sitemapRoutes, SITE_NAME, HOME_DESCRIPTION } from '../web/js/meta.js';
import {
  brandLink, footerContent, homeContent, fruitContent, fruitIntro, aboutContent, privacyContent,
} from '../web/js/pages.js';

export const WEB_ROOT = join(dirname(fileURLToPath(import.meta.url)), '..', 'web');

// Public by design: Bing and the other IndexNow engines fetch /<key>.txt to
// check that a "these pages changed" ping really comes from this site.
export const INDEXNOW_KEY = 'f3a85b26e985395e297b57d8c8270284';

// Proves to Bing Webmaster Tools that the site is ours. Must stay in the page.
const BING_VERIFICATION = '98C3F99FD624BD0518FA653152FE9904';

// The year in the footer of the static pages. app.js shows the current year.
const FOOTER_YEAR = 2026;

/* ---------- an `h` that builds HTML text (same contract as the one in app.js) ---------- */

class Html {
  constructor(value) { this.value = value; }
}

const VOID_TAGS = new Set(['img', 'input', 'meta', 'link', 'br']);

function escapeText(value) {
  return String(value).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

function escapeAttribute(value) {
  return escapeText(value).replace(/"/g, '&quot;');
}

function h(tag, props, ...children) {
  let attributes = '';
  let text = '';
  for (const [key, value] of Object.entries(props || {})) {
    if (value === false || value === null || value === undefined) continue;
    if (key === 'text') text = escapeText(value);
    else if (key.startsWith('on')) continue; // event handlers exist only in the browser
    else if (value === true) attributes += ` ${key}`;
    else attributes += ` ${key}="${escapeAttribute(value)}"`;
  }
  if (VOID_TAGS.has(tag)) return new Html(`<${tag}${attributes}>`);
  const inner = children.flat(Infinity)
    .filter((child) => child !== null && child !== undefined && child !== false)
    .map((child) => (child instanceof Html ? child.value : escapeText(child)))
    .join('');
  return new Html(`<${tag}${attributes}>${text}${inner}</${tag}>`);
}

function html(nodes) {
  return [nodes].flat(Infinity).map((node) => node.value).join('\n    ');
}

/* ---------- the page template ---------- */

function jsonLd(data) {
  // "<" is escaped so the data can never close the script tag.
  return `<script type="application/ld+json">${JSON.stringify(data).replace(/</g, '\\u003c')}</script>`;
}

/**
 * route:       a route as app.js understands it (decides title, description, canonical)
 * main:        finished HTML for <main>, or '' for pages only JavaScript can fill
 * structured:  JSON-LD for the page, if any
 * needsScript: adds the "turn JavaScript on" note for pages that load Bible text
 * generic:     true for the fallback shell, which serves many addresses and so
 *              carries no canonical address of its own
 */
function page({ route, main = '', structured = null, needsScript = true, generic = false }) {
  const meta = generic
    ? { title: SITE_NAME, description: HOME_DESCRIPTION, path: null, index: true }
    : pageMeta(route);
  const url = meta.path ? SITE_ORIGIN + meta.path : null;
  const head = [
    '<meta charset="utf-8">',
    '<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">',
    `<title>${escapeText(meta.title)}</title>`,
    `<meta name="description" content="${escapeAttribute(meta.description)}">`,
    meta.index ? null : '<meta name="robots" content="noindex">',
    url ? `<link rel="canonical" href="${escapeAttribute(url)}">` : null,
    '<meta name="theme-color" content="#F5F8F7" media="(prefers-color-scheme: light)">',
    '<meta name="theme-color" content="#0F1A1D" media="(prefers-color-scheme: dark)">',
    '<meta name="apple-itunes-app" content="app-id=6497410297">',
    `<meta name="msvalidate.01" content="${BING_VERIFICATION}">`,
    '',
    `<meta property="og:site_name" content="${SITE_NAME}">`,
    `<meta property="og:title" content="${escapeAttribute(meta.title)}">`,
    `<meta property="og:description" content="${escapeAttribute(meta.description)}">`,
    '<meta property="og:type" content="website">',
    url ? `<meta property="og:url" content="${escapeAttribute(url)}">` : null,
    `<meta property="og:image" content="${SITE_ORIGIN}/img/og.jpg">`,
    '<meta name="twitter:card" content="summary">',
    '',
    '<link rel="icon" type="image/png" href="/img/favicon.png">',
    '<link rel="apple-touch-icon" href="/img/apple-touch-icon.png">',
    '<link rel="manifest" href="/manifest.webmanifest">',
    '<link rel="preload" href="/fonts/gentium-book-plus-400.woff2" as="font" type="font/woff2" crossorigin>',
    '<link rel="stylesheet" href="/styles.css">',
    structured ? jsonLd(structured) : null,
    '<script src="/js/boot.js"></script>',
    '<script type="module" src="/js/app.js"></script>',
  ].filter((line) => line !== null);

  const viewClass = generic ? '' : ` class="view view-${route.name}"`;
  const body = [
    '<a class="skip-link" href="#main">Skip to the text</a>',
    `<header class="bar" id="bar">${brandLink(h).value}</header>`,
    main ? `<main id="main" tabindex="-1"${viewClass}>\n    ${main}\n  </main>` : `<main id="main" tabindex="-1"${viewClass}></main>`,
    `<footer class="foot" id="foot">\n    ${html(footerContent(h, FOOTER_YEAR))}\n  </footer>`,
    '<div class="toast" id="toast" role="status" aria-live="polite"></div>',
    needsScript ? '<noscript>\n    <p class="noscript">Scroll needs JavaScript turned on to load Bible text.</p>\n  </noscript>' : null,
  ].filter((line) => line !== null);

  return [
    '<!doctype html>',
    '<!-- Written by scripts/build-pages.mjs. Edit web/js/pages.js or web/js/meta.js, then run it again. -->',
    '<html lang="en">',
    '<head>',
    ...head.map((line) => (line ? `  ${line}` : '')),
    '</head>',
    '<body>',
    ...body.map((line) => `  ${line}`),
    '</body>',
    '</html>',
    '',
  ].join('\n');
}

/* ---------- the pages ---------- */

const SITE_ID = `${SITE_ORIGIN}/#website`;
const PUBLISHER_ID = `${SITE_ORIGIN}/#publisher`;

function homeStructuredData() {
  return {
    '@context': 'https://schema.org',
    '@graph': [
      {
        '@type': 'WebSite',
        '@id': SITE_ID,
        url: `${SITE_ORIGIN}/`,
        name: SITE_NAME,
        description: HOME_DESCRIPTION,
        inLanguage: 'en',
        publisher: { '@id': PUBLISHER_ID },
      },
      { '@type': 'Organization', '@id': PUBLISHER_ID, name: 'Okeke LLC', url: 'https://okeke.us/' },
    ],
  };
}

function fruitStructuredData(fruit) {
  return {
    '@context': 'https://schema.org',
    '@type': 'BreadcrumbList',
    itemListElement: [
      { '@type': 'ListItem', position: 1, name: SITE_NAME, item: `${SITE_ORIGIN}/` },
      { '@type': 'ListItem', position: 2, name: `Bible verses about ${fruit.term}`, item: `${SITE_ORIGIN}/fruit/${fruit.slug}` },
    ],
  };
}

function sitemap() {
  const urls = sitemapRoutes().map((route) => `  <url><loc>${SITE_ORIGIN}${pageMeta(route).path}</loc></url>`);
  return [
    '<?xml version="1.0" encoding="UTF-8"?>',
    '<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">',
    ...urls,
    '</urlset>',
    '',
  ].join('\n');
}

/** Every generated file: path under web/ → contents. */
export function buildFiles() {
  const files = new Map();

  files.set('index.html', page({
    route: { name: 'home' },
    main: html(homeContent(h, { start: { href: '/ESV/1/1/', text: 'Start at Genesis 1' } })),
    structured: homeStructuredData(),
  }));

  for (const fruit of FRUITS) {
    files.set(`fruit/${fruit.slug}.html`, page({
      route: { name: 'fruit', fruit },
      main: html(fruitContent(h, fruit, {
        lede: h('p', { class: 'page-lede', text: fruitIntro(fruit) }),
        shuffleButton: null,
        results: h('div', { class: 'results' }),
      })),
      structured: fruitStructuredData(fruit),
    }));
  }

  files.set('about.html', page({ route: { name: 'about' }, main: html(aboutContent(h)), needsScript: false }));
  files.set('privacy.html', page({ route: { name: 'privacy' }, main: html(privacyContent(h)), needsScript: false }));

  // Pages only JavaScript can fill. Search and Saved are kept out of search results.
  files.set('search.html', page({ route: { name: 'search', query: '' } }));
  files.set('saved.html', page({ route: { name: 'saved' } }));

  // What the host serves for every other address (chapters, verses, mistyped
  // links). See navigationFallback in staticwebapp.config.json.
  files.set('shell.html', page({ generic: true }));

  files.set('sitemap.xml', sitemap());
  files.set('robots.txt', `User-agent: *\nAllow: /\n\nSitemap: ${SITE_ORIGIN}/sitemap.xml\n`);
  files.set(`${INDEXNOW_KEY}.txt`, INDEXNOW_KEY); // the key and nothing else, not even a newline

  return files;
}

/** Names of the generated files that differ from what is on disk. */
export async function staleFiles() {
  const stale = [];
  for (const [name, content] of buildFiles()) {
    const onDisk = await readFile(join(WEB_ROOT, name), 'utf8').catch(() => null);
    if (onDisk !== content) stale.push(name);
  }
  return stale;
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  if (process.argv.includes('--check')) {
    const stale = await staleFiles();
    if (stale.length) {
      console.error(`Out of date: ${stale.join(', ')}\nRun: node scripts/build-pages.mjs`);
      process.exit(1);
    }
    console.log('Static pages are up to date.');
  } else {
    const files = buildFiles();
    for (const [name, content] of files) {
      const path = join(WEB_ROOT, name);
      await mkdir(dirname(path), { recursive: true });
      await writeFile(path, content);
    }
    console.log(`Wrote ${files.size} files under web/.`);
  }
}
