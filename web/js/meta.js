// Title, description and canonical address for every page. Used twice:
// app.js applies it in the browser on each navigation, and
// scripts/build-pages.mjs writes it into the static pages so search engines
// see it without running JavaScript.

import { TRANSLATIONS, FRUITS } from './bible.js';
import { reference, chapterPath } from './format.js';

export const SITE_NAME = 'Scroll';

export const HOME_DESCRIPTION = 'Read the Bible a chapter at a time, find verses on the Fruits of the Spirit, and share the ones that stay with you. Free, no account.';

function translationName(code) {
  const found = TRANSLATIONS.find((t) => t.code === code);
  return found ? found.name : code;
}

/**
 * `route` is what parseRoute() returns in app.js. Returns
 * { title, description, path, index }: `path` is the canonical path, and is
 * null for pages search engines should leave out (`index` false).
 */
export function pageMeta(route) {
  switch (route.name) {
    case 'home':
      return {
        title: 'Scroll: Bible Verses on the Fruit of the Spirit',
        description: HOME_DESCRIPTION,
        path: '/',
        index: true,
      };
    case 'fruit':
      return {
        title: `Bible Verses About ${route.fruit.name} | ${SITE_NAME}`,
        description: `Bible verses about ${route.fruit.term}, one of the Fruits of the Spirit in Galatians 5:22-23. Read them in the ESV, KJV, NASB or NLT, and save or share them.`,
        path: `/fruit/${route.fruit.slug}`,
        index: true,
      };
    case 'reader': {
      // A verse address (/ESV/43/3/16) is the same page as its chapter.
      const where = reference(route.book, route.chapter);
      return {
        title: `${where} ${route.translation} | ${SITE_NAME}`,
        description: `Read ${where} in the ${translationName(route.translation)} (${route.translation}). Tap a verse to save it or share it with a link.`,
        path: chapterPath(route.translation, route.book, route.chapter),
        index: true,
      };
    }
    case 'about':
      return {
        title: `About | ${SITE_NAME}`,
        description: 'Scroll is a quiet place to read the Bible, built around the Fruits of the Spirit in Galatians 5. Free, with no account, no ads and no tracking.',
        path: '/about',
        index: true,
      };
    case 'privacy':
      return {
        title: `Privacy | ${SITE_NAME}`,
        description: 'Privacy policy for the Scroll website and the Scroll apps for iPhone, iPad and Android. Scroll collects nothing.',
        path: '/privacy',
        index: true,
      };
    case 'search':
      return {
        title: `${route.query ? `“${route.query}”` : 'Search'} | ${SITE_NAME}`,
        description: 'Search every verse in the Bible for a word or phrase.',
        path: null,
        index: false,
      };
    case 'saved':
      return {
        title: `Saved | ${SITE_NAME}`,
        description: 'The verses and chapters you saved in this browser.',
        path: null,
        index: false,
      };
    default:
      return {
        title: `Page not found | ${SITE_NAME}`,
        description: HOME_DESCRIPTION,
        path: null,
        index: false,
      };
  }
}

/** The pages listed in sitemap.xml, as routes. Chapters are reached by links, not listed. */
export function sitemapRoutes() {
  return [
    { name: 'home' },
    ...FRUITS.map((fruit) => ({ name: 'fruit', fruit })),
    { name: 'about' },
    { name: 'privacy' },
  ];
}
