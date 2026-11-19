// Page content that is written once and built twice. Every function takes `h`,
// the element builder: app.js passes one that makes DOM nodes, and
// scripts/build-pages.mjs passes one that makes HTML text for the static pages.
// Event handlers (onClick, onSubmit) only exist in the browser.

import { TRANSLATIONS, FRUITS, COPYRIGHT } from './bible.js';

export const APP_STORE_URL = 'https://apps.apple.com/app/id6497410297';
export const PLAY_STORE_URL = 'https://play.google.com/store/apps/details?id=com.christianokeke.scroll';
export const CONTACT_EMAIL = 'christian@okeke.us';

export function brandLink(h) {
  // The word "Scroll" is hidden on narrow screens, so the link names itself.
  return h('a', { class: 'brand', href: '/', 'aria-label': 'Scroll home' },
    h('img', { src: '/img/logo.jpg', alt: '', width: '30', height: '30' }),
    h('span', { text: 'Scroll' }));
}

export function footerContent(h, year) {
  return [
    h('nav', { class: 'foot-links', 'aria-label': 'Site' },
      h('a', { href: '/about', text: 'About' }),
      h('a', { href: '/privacy', text: 'Privacy' }),
      h('a', { href: APP_STORE_URL, rel: 'noopener', text: 'iPhone app' }),
      h('a', { href: PLAY_STORE_URL, rel: 'noopener', text: 'Android app' })),
    h('p', { class: 'foot-note' }, `© ${year} `, h('a', { href: 'https://okeke.us/', text: 'Okeke LLC' })),
  ];
}

/**
 * Home page. `start` is the main button ({ href, text }): "Start at Genesis 1",
 * or "Continue reading …" for someone who has been here before.
 */
export function homeContent(h, { start, onPick, onSearch }) {
  // Galatians 5:22-23 (ESV). Each fruit in the verse opens its verses.
  const fruitLink = (slug) => {
    const fruit = FRUITS.find((item) => item.slug === slug);
    return h('a', { class: 'fruit-link', href: `/fruit/${fruit.slug}`, text: fruit.word });
  };
  const verse = h('blockquote', { class: 'hero-verse' },
    h('p', null,
      'But the fruit of the Spirit is ', fruitLink('love'), ', ', fruitLink('joy'), ', ', fruitLink('peace'), ', ',
      fruitLink('patience'), ', ', fruitLink('kindness'), ', ', fruitLink('goodness'), ', ', fruitLink('faith'), ', ',
      fruitLink('gentleness'), ', ', fruitLink('self-control'), '; against such things there is no law.'),
    h('footer', null,
      h('a', { class: 'hero-ref', href: '/ESV/48/5/22-23', text: 'Galatians 5:22-23' })));

  // Without JavaScript the form still works: it opens /search?q=…
  const search = h('form', { class: 'home-search', role: 'search', action: '/search', method: 'get', onSubmit: onSearch },
    h('input', { class: 'field', type: 'search', name: 'q', placeholder: 'A word or phrase', 'aria-label': 'Search the Bible', autocomplete: 'off' }),
    h('button', { type: 'submit', class: 'btn', text: 'Search the Bible' }));

  return [
    h('section', { class: 'hero' },
      h('h1', { class: 'sr-only', text: 'Scroll: read the Bible and find verses on the Fruits of the Spirit' }),
      verse,
      h('p', { class: 'hero-hint', text: 'Choose a word above to read verses about it.' })),
    h('section', { class: 'home-read', 'aria-label': 'Read the Bible' },
      h('div', { class: 'home-actions' },
        h('a', { class: 'btn btn-primary', href: start.href, text: start.text }),
        h('button', { type: 'button', class: 'btn', onClick: onPick, text: 'Choose a book' })),
      search),
    h('section', { class: 'home-app' },
      h('img', { src: '/img/logo.jpg', alt: '', width: '56', height: '56' }),
      h('p', null,
        'Scroll is also an app for iPhone, iPad and Android. Free, with no account and no ads. Get Scroll ',
        h('a', { href: APP_STORE_URL, rel: 'noopener', text: 'on the App Store' }), ' or ',
        h('a', { href: PLAY_STORE_URL, rel: 'noopener', text: 'on Google Play' }), '.')),
  ];
}

/** The line under a fruit's heading until its verses have loaded. */
export function fruitIntro(fruit) {
  return `Bible verses that mention ${fruit.term}, one of the Fruits of the Spirit in Galatians 5:22-23.`;
}

/**
 * A Fruit of the Spirit page. The caller supplies the parts it fills in later:
 * `lede` (the line under the heading), `shuffleButton` and `results`.
 */
export function fruitContent(h, fruit, { lede, shuffleButton, results }) {
  return [
    h('header', { class: 'page-head' },
      h('h1', { class: 'page-title', text: fruit.name }),
      lede,
      h('div', { class: 'page-tools' },
        shuffleButton,
        h('nav', { class: 'fruit-nav', 'aria-label': 'Fruits of the Spirit' },
          FRUITS.filter((item) => item.slug !== fruit.slug).map((item) => h('a', { href: `/fruit/${item.slug}`, text: item.name }))))),
    results,
  ];
}

function prose(h, title, ...blocks) {
  return h('article', { class: 'prose' }, h('h1', { class: 'page-title', text: title }), ...blocks);
}

export function aboutContent(h) {
  return prose(h, 'About Scroll',
    h('p', { text: 'Scroll is a quiet place to read the Bible. Open any chapter, pick the translation you like, save what you want to come back to, and share a verse with a link that opens right on it.' }),
    h('p', null,
      'It began as an iPhone app built around the Fruits of the Spirit in Galatians 5: love, joy, peace, patience, kindness, goodness, faithfulness, gentleness and self-control. The home page still starts there. Scroll is free ',
      h('a', { href: APP_STORE_URL, rel: 'noopener', text: 'on the App Store' }), ' and ',
      h('a', { href: PLAY_STORE_URL, rel: 'noopener', text: 'on Google Play' }), '.'),
    h('p', { text: 'There is no account, no ads and no tracking. Anything you save stays on your device.' }),
    h('h2', { text: 'Translations' }),
    ...TRANSLATIONS.map((t) => h('p', { class: 'prose-small' }, h('strong', { text: `${t.name} (${t.code}). ` }), COPYRIGHT[t.code])),
    h('p', { class: 'prose-small' }, 'Bible text is loaded from ', h('a', { href: 'https://bolls.life', rel: 'noopener', text: 'bolls.life' }), '.'),
    h('h2', { text: 'Contact' }),
    h('p', null, 'Questions or a problem with the site? Email ', h('a', { href: `mailto:${CONTACT_EMAIL}`, text: CONTACT_EMAIL }), '.'));
}

export function privacyContent(h) {
  return prose(h, 'Privacy',
    h('p', { text: 'This covers the Scroll website and the Scroll apps for iPhone, iPad and Android. Scroll is made by Okeke LLC.' }),
    h('h2', { text: 'What Scroll collects' }),
    h('p', { text: 'Nothing. There are no accounts, no ads, no analytics and no tracking. Scroll does not ask for your name, email, contacts or location.' }),
    h('h2', { text: 'What stays on your device' }),
    h('p', { text: 'Verses and chapters you save, the translation you last used, and your text size and theme are stored only on your device (in the app, or in this browser for the website). They are never sent to us. Deleting the app or clearing this site’s data in your browser removes them.' }),
    h('h2', { text: 'Where Bible text comes from' }),
    h('p', null,
      'When you open a chapter or search, Scroll requests the text from ',
      h('a', { href: 'https://bolls.life', rel: 'noopener', text: 'bolls.life' }),
      ', a public Bible service. Like any website, that service and our web host (Microsoft Azure) can see technical details of the request, such as your IP address and which chapter was requested. Scroll adds no identifiers to these requests.'),
    h('h2', { text: 'Sharing' }),
    h('p', { text: 'When you share or copy a verse, Scroll hands the text and a link to the app you choose. Nothing is shared unless you do it.' }),
    h('h2', { text: 'Children' }),
    h('p', { text: 'Scroll is suitable for all ages and collects no personal information from anyone.' }),
    h('h2', { text: 'Contact' }),
    h('p', null, 'Questions about privacy? Email ', h('a', { href: `mailto:${CONTACT_EMAIL}`, text: CONTACT_EMAIL }), '.'),
    h('p', { class: 'prose-small', text: 'Last updated November 16, 2026.' }));
}
