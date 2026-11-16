import {
  TRANSLATIONS, BOOKS, OLD_TESTAMENT_BOOKS, FRUITS, COPYRIGHT,
  bookName, chaptersIn, isValidLocation, isTranslation, resolveTranslation,
  nextChapter, previousChapter,
} from './bible.js';
import {
  reference, chapterPath, versePath, parseVerseSpec, shareParts,
} from './format.js';
import { fetchChapter, prefetchChapter, findAllVerses, searchVerses } from './api.js';
import { store } from './store.js';

const APP_STORE_URL = 'https://apps.apple.com/app/id6497410297';
const CONTACT_EMAIL = 'christian@okeke.us';

const $bar = document.getElementById('bar');
const $main = document.getElementById('main');
const $foot = document.getElementById('foot');
const $toast = document.getElementById('toast');

/* ---------- small DOM helpers ---------- */

function h(tag, props, ...children) {
  const el = document.createElement(tag);
  for (const [key, value] of Object.entries(props || {})) {
    if (value === false || value === null || value === undefined) continue;
    if (key === 'class') el.className = value;
    else if (key === 'text') el.textContent = value;
    else if (key.startsWith('on')) el.addEventListener(key.slice(2).toLowerCase(), value);
    else if (value === true) el.setAttribute(key, '');
    else el.setAttribute(key, value);
  }
  el.append(...children.flat(Infinity).filter((child) => child !== null && child !== undefined && child !== false));
  return el;
}

const ICONS = {
  search: '<circle cx="11" cy="11" r="7"/><path d="m20 20-3.6-3.6"/>',
  bookmark: '<path d="M6.5 4h11v16.5L12 16.6l-5.500 3.900z"/>',
  share: '<path d="M12 15V3.500"/><path d="m7.500 8 4.500-4.500L16.500 8"/><path d="M5 12.500V19a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-6.500"/>',
  copy: '<rect x="9" y="9" width="11" height="11" rx="2"/><path d="M5 15V6a2 2 0 0 1 2-2h9"/>',
  shuffle: '<path d="M3 7h3.200c1.800 0 3.300.900 4.200 2.400l3.200 5.200c.900 1.500 2.400 2.400 4.200 2.400H21"/><path d="M3 17h3.200c1.200 0 2.300-.400 3.100-1.200"/><path d="M21 7h-3.200c-1.200 0-2.300.400-3.100 1.200"/><path d="m18 4 3 3-3 3"/><path d="m18 14 3 3-3 3"/>',
  left: '<path d="m15 5-7 7 7 7"/>',
  right: '<path d="m9 5 7 7-7 7"/>',
  down: '<path d="m6 9 6 6 6-6"/>',
  close: '<path d="M6 6l12 12"/><path d="M18 6 6 18"/>',
};

function icon(name) {
  const template = document.createElement('template');
  template.innerHTML = `<svg class="icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false">${ICONS[name]}</svg>`;
  return template.content.firstChild;
}

let toastTimer;
function toast(message) {
  clearTimeout(toastTimer);
  $toast.textContent = message;
  $toast.classList.add('is-visible');
  toastTimer = setTimeout(() => $toast.classList.remove('is-visible'), 2400);
}

async function copyText(text) {
  try {
    await navigator.clipboard.writeText(text);
    return true;
  } catch {
    const area = h('textarea', { class: 'sr-only', readonly: true });
    area.value = text;
    document.body.append(area);
    area.select();
    let ok = false;
    try { ok = document.execCommand('copy'); } catch { ok = false; }
    area.remove();
    return ok;
  }
}

/** Opens the system share sheet where there is one; otherwise copies text and link. */
async function shareVerses(payload) {
  const parts = shareParts(payload);
  if (navigator.share) {
    try {
      await navigator.share({ text: parts.body, url: parts.url });
      return;
    } catch (error) {
      if (error && error.name === 'AbortError') return;
    }
  }
  toast((await copyText(parts.full)) ? 'Copied with a link' : 'Could not copy');
}

async function copyVerses(payload) {
  toast((await copyText(shareParts(payload).full)) ? 'Copied' : 'Could not copy');
}

function shuffled(list) {
  const copy = [...list];
  for (let i = copy.length - 1; i > 0; i -= 1) {
    const j = Math.floor(Math.random() * (i + 1));
    [copy[i], copy[j]] = [copy[j], copy[i]];
  }
  return copy;
}

/* ---------- routing ---------- */

function parseRoute() {
  const segments = location.pathname.split('/').filter(Boolean).map((part) => {
    try { return decodeURIComponent(part); } catch { return part; }
  });
  if (!segments.length) return { name: 'home' };

  const [first, second, third, fourth] = segments;
  if (segments.length === 1) {
    if (first === 'saved') return { name: 'saved' };
    if (first === 'about') return { name: 'about' };
    if (first === 'privacy') return { name: 'privacy' };
    if (first === 'search') {
      return { name: 'search', query: (new URLSearchParams(location.search).get('q') || '').trim() };
    }
  }
  if (first === 'fruit' && segments.length === 2) {
    const fruit = FRUITS.find((item) => item.slug === second.toLowerCase());
    return fruit ? { name: 'fruit', fruit } : { name: 'notfound' };
  }
  // /ESV/43/3/  and  /ESV/43/3/16  and  /ESV/43/3/16-18  (same shape as bolls.life)
  if (segments.length >= 2 && segments.length <= 4 && /^[A-Za-z0-9]{2,10}$/.test(first)
      && /^\d{1,2}$/.test(second) && (third === undefined || /^\d{1,3}$/.test(third))) {
    const book = Number(second);
    const chapter = third === undefined ? 1 : Number(third);
    if (!isValidLocation(book, chapter)) return { name: 'notfound' };
    return {
      name: 'reader',
      translation: resolveTranslation(first),
      requestedTranslation: first,
      book,
      chapter,
      verses: parseVerseSpec(fourth),
    };
  }
  return { name: 'notfound' };
}

function navigate(path, { replace = false } = {}) {
  if (replace) history.replaceState(null, '', path);
  else history.pushState(null, '', path);
  render();
}

document.addEventListener('click', (event) => {
  if (event.defaultPrevented || event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
  const link = event.target.closest('a[href]');
  if (!link || link.target === '_blank' || link.hasAttribute('download')) return;
  const url = new URL(link.href, location.href);
  if (url.origin !== location.origin) return;
  event.preventDefault();
  if (url.pathname + url.search !== location.pathname + location.search) navigate(url.pathname + url.search);
  else window.scrollTo({ top: 0 });
});

window.addEventListener('popstate', () => render());

/* ---------- per-view lifecycle ---------- */

let viewAbort = new AbortController();
let viewCleanups = [];
let currentRoute = { name: 'home' };

function onLeave(fn) {
  viewCleanups.push(fn);
}

function setTitle(title) {
  document.title = title ? `${title} | Scroll` : 'Scroll';
}

function render() {
  viewAbort.abort();
  viewAbort = new AbortController();
  viewCleanups.forEach((fn) => fn());
  viewCleanups = [];
  closePicker();
  closeMenu();

  const route = parseRoute();
  currentRoute = route;

  if (route.name === 'reader') {
    // Canonical address: offered translation, upper case, trailing slash on chapters.
    const canonical = route.verses.length
      ? versePath(route.translation, route.book, route.chapter, route.verses)
      : chapterPath(route.translation, route.book, route.chapter);
    if (location.pathname !== canonical) history.replaceState(null, '', canonical);
  }

  $main.replaceChildren();
  $main.className = `view view-${route.name}`;
  window.scrollTo(0, 0);
  updateBar();

  const views = {
    home: renderHome,
    reader: renderReader,
    fruit: renderFruit,
    search: renderSearch,
    saved: renderSaved,
    about: renderAbout,
    privacy: renderPrivacy,
    notfound: renderNotFound,
  };
  views[route.name](route);
}

/** The translation in effect: the one in the address on reader pages, the remembered one elsewhere. */
function activeTranslation() {
  return currentRoute.name === 'reader' ? currentRoute.translation : store.translation();
}

/* ---------- top bar ---------- */

let $location;
let $translation;
let $savedCount;
let $menu;

function buildBar() {
  $location = h('button', { class: 'bar-location', type: 'button', onClick: () => openPicker() });

  $translation = h('select', {
    class: 'bar-select',
    'aria-label': 'Translation',
    onChange: (event) => {
      const code = event.target.value;
      store.setTranslation(code);
      if (currentRoute.name === 'reader') {
        const { book, chapter, verses } = currentRoute;
        navigate(verses.length ? versePath(code, book, chapter, verses) : chapterPath(code, book, chapter));
      } else if (currentRoute.name === 'fruit' || currentRoute.name === 'search') {
        render();
      }
    },
  }, TRANSLATIONS.map((t) => h('option', { value: t.code, text: t.code, title: t.name })));

  $savedCount = h('span', { class: 'bar-count' });

  $menu = h('details', { class: 'menu' },
    h('summary', { class: 'bar-button', 'aria-label': 'Text size and theme', title: 'Text size and theme' },
      h('span', { class: 'menu-glyph', 'aria-hidden': 'true', text: 'Aa' })),
    buildMenuPanel());

  $bar.replaceChildren(
    h('a', { class: 'brand', href: '/' },
      h('img', { src: '/img/logo.jpg', alt: '', width: '30', height: '30' }),
      h('span', { text: 'Scroll' })),
    $location,
    $translation,
    h('span', { class: 'bar-gap' }),
    h('a', { class: 'bar-button', href: '/search', 'aria-label': 'Search', title: 'Search' }, icon('search')),
    h('a', { class: 'bar-button bar-saved', href: '/saved', title: 'Saved' },
      icon('bookmark'), h('span', { class: 'sr-only', text: 'Saved' }), $savedCount),
    $menu,
  );
}

function updateBar() {
  const onReader = currentRoute.name === 'reader';
  $location.replaceChildren(
    h('span', { text: onReader ? reference(currentRoute.book, currentRoute.chapter) : 'Books' }),
    icon('down'),
  );
  $location.setAttribute('aria-label', onReader
    ? `${reference(currentRoute.book, currentRoute.chapter)}. Choose a book and chapter`
    : 'Choose a book and chapter');
  $translation.value = activeTranslation();
  updateSavedCount();
}

function updateSavedCount() {
  const count = store.savedCount();
  $savedCount.textContent = count ? String(count) : '';
  $savedCount.hidden = !count;
}

window.addEventListener('scroll:store', updateSavedCount);
window.addEventListener('storage', updateSavedCount);

/* ---------- text size and theme menu ---------- */

const TEXT_SIZE_NAMES = ['Small', 'Medium', 'Large', 'Largest'];

function applyDisplay() {
  const root = document.documentElement;
  const theme = store.theme();
  if (theme === 'system') root.removeAttribute('data-theme');
  else root.setAttribute('data-theme', theme);
  const size = store.textSize();
  if (size === 1) root.removeAttribute('data-text-size');
  else root.setAttribute('data-text-size', String(size));
}

function buildMenuPanel() {
  const sizeLabel = h('span', { class: 'menu-value', text: TEXT_SIZE_NAMES[store.textSize()] });
  const stepSize = (delta) => {
    const size = Math.min(3, Math.max(0, store.textSize() + delta));
    store.setTextSize(size);
    sizeLabel.textContent = TEXT_SIZE_NAMES[size];
    applyDisplay();
  };

  const themeButtons = ['system', 'light', 'dark'].map((theme) => h('button', {
    type: 'button',
    class: 'segment',
    'aria-pressed': String(store.theme() === theme),
    text: theme === 'system' ? 'Auto' : theme[0].toUpperCase() + theme.slice(1),
    onClick: () => {
      store.setTheme(theme);
      applyDisplay();
      themeButtons.forEach((button, index) => {
        button.setAttribute('aria-pressed', String(['system', 'light', 'dark'][index] === theme));
      });
    },
  }));

  return h('div', { class: 'menu-panel' },
    h('div', { class: 'menu-row' },
      h('span', { class: 'menu-label', text: 'Text size' }),
      h('div', { class: 'stepper' },
        h('button', { type: 'button', class: 'step', 'aria-label': 'Smaller text', onClick: () => stepSize(-1), text: 'A' }),
        sizeLabel,
        h('button', { type: 'button', class: 'step step-large', 'aria-label': 'Larger text', onClick: () => stepSize(1), text: 'A' }))),
    h('div', { class: 'menu-row' },
      h('span', { class: 'menu-label', text: 'Theme' }),
      h('div', { class: 'segments' }, themeButtons)));
}

function closeMenu() {
  if ($menu) $menu.open = false;
}

document.addEventListener('click', (event) => {
  if ($menu && $menu.open && !$menu.contains(event.target)) closeMenu();
});

/* ---------- book and chapter picker ---------- */

let $picker;
let $pickerBody;
let $pickerFilter;

function buildPicker() {
  $pickerFilter = h('input', {
    class: 'picker-filter',
    type: 'search',
    placeholder: 'Find a book',
    'aria-label': 'Find a book',
    autocomplete: 'off',
    onInput: () => showBooks(),
    onKeydown: (event) => {
      if (event.key !== 'Enter') return;
      const first = $pickerBody.querySelector('.picker-book');
      if (first) first.click();
    },
  });
  $pickerBody = h('div', { class: 'picker-body' });
  $picker = h('dialog', { class: 'picker', 'aria-label': 'Choose a book and chapter' },
    h('div', { class: 'picker-head' },
      $pickerFilter,
      h('button', { type: 'button', class: 'bar-button', 'aria-label': 'Close', onClick: () => closePicker() }, icon('close'))),
    $pickerBody);
  // A click on the backdrop (the dialog element itself) closes it.
  $picker.addEventListener('click', (event) => {
    if (event.target === $picker) closePicker();
  });
  document.body.append($picker);
}

function showBooks() {
  const filter = $pickerFilter.value.trim().toLowerCase();
  const matches = (name) => !filter || name.toLowerCase().replace(/\s+/g, '').includes(filter.replace(/\s+/g, ''));
  const section = (title, from, to) => {
    const books = BOOKS.map((name, index) => ({ name, book: index + 1 }))
      .slice(from, to)
      .filter((entry) => matches(entry.name));
    if (!books.length) return null;
    return h('section', { class: 'picker-section' },
      h('h2', { class: 'picker-heading', text: title }),
      h('div', { class: 'picker-books' }, books.map((entry) => h('button', {
        type: 'button',
        class: `picker-book${currentRoute.name === 'reader' && currentRoute.book === entry.book ? ' is-current' : ''}`,
        text: entry.name,
        onClick: () => {
          if (chaptersIn(entry.book) === 1) navigate(chapterPath(activeTranslation(), entry.book, 1));
          else showChapters(entry.book);
        },
      }))));
  };
  const sections = [section('Old Testament', 0, OLD_TESTAMENT_BOOKS), section('New Testament', OLD_TESTAMENT_BOOKS, BOOKS.length)].filter(Boolean);
  $pickerBody.replaceChildren(...(sections.length
    ? sections
    : [h('p', { class: 'picker-empty', text: `No book matches “${$pickerFilter.value.trim()}”.` })]));
}

function showChapters(book) {
  const translation = activeTranslation();
  const back = h('button', { type: 'button', class: 'picker-back', onClick: () => { showBooks(); $pickerFilter.focus(); } },
    icon('left'), h('span', { text: 'All books' }));
  const links = Array.from({ length: chaptersIn(book) }, (_, index) => {
    const chapter = index + 1;
    const isCurrent = currentRoute.name === 'reader' && currentRoute.book === book && currentRoute.chapter === chapter;
    return h('a', { class: `picker-chapter${isCurrent ? ' is-current' : ''}`, href: chapterPath(translation, book, chapter), text: String(chapter) });
  });
  $pickerBody.replaceChildren(
    back,
    h('h2', { class: 'picker-title', text: bookName(book) }),
    h('div', { class: 'picker-chapters' }, links));
  links[0].focus();
}

function openPicker() {
  if (!$picker) buildPicker();
  $pickerFilter.value = '';
  showBooks();
  $picker.showModal();
  // On touch screens focusing the field would pop the keyboard over the list.
  if (window.matchMedia('(hover: hover)').matches) $pickerFilter.focus();
  else $picker.focus();
}

function closePicker() {
  if ($picker && $picker.open) $picker.close();
}

/* ---------- shared pieces ---------- */

function loadingNote(text) {
  // Held back briefly so fast loads do not flash a message.
  const note = h('p', { class: 'status status-loading', text, hidden: true });
  const timer = setTimeout(() => { note.hidden = false; }, 250);
  onLeave(() => clearTimeout(timer));
  return note;
}

function errorNote(message, retry) {
  return h('div', { class: 'status status-error' },
    h('p', { text: message }),
    h('button', { type: 'button', class: 'btn', onClick: retry, text: 'Try again' }));
}

function loadFailedMessage(what) {
  return navigator.onLine === false
    ? `You are offline, so ${what} could not load.`
    : `${what[0].toUpperCase()}${what.slice(1)} could not load. Check your connection.`;
}

/** One verse in a list (Fruits of the Spirit, search, saved). */
function passage(verse, { removable = false, onRemoved } = {}) {
  const payload = { text: verse.text, translation: verse.translation, book: verse.book, chapter: verse.chapter, verses: [verse.verse] };

  const saveButton = h('button', { type: 'button', class: 'btn btn-quiet' });
  const paintSave = () => {
    const saved = store.isVerseSaved(verse);
    saveButton.replaceChildren(icon('bookmark'), h('span', { text: removable ? 'Remove' : saved ? 'Saved' : 'Save' }));
    saveButton.classList.toggle('is-on', saved && !removable);
    if (!removable) saveButton.setAttribute('aria-pressed', String(saved));
  };
  saveButton.addEventListener('click', () => {
    if (removable || store.isVerseSaved(verse)) {
      store.removeVerse(verse);
      toast('Removed from saved');
      if (removable && onRemoved) { onRemoved(); return; }
    } else {
      store.saveVerse(verse);
      toast('Saved');
    }
    paintSave();
  });
  paintSave();

  return h('article', { class: 'passage' },
    h('p', { class: 'passage-text', text: verse.text }),
    h('div', { class: 'passage-meta' },
      h('a', { class: 'passage-ref', href: versePath(verse.translation, verse.book, verse.chapter, [verse.verse]) },
        `${reference(verse.book, verse.chapter, [verse.verse])} `,
        h('span', { class: 'passage-translation', text: verse.translation })),
      h('div', { class: 'passage-actions' },
        h('button', { type: 'button', class: 'btn btn-quiet', onClick: () => shareVerses(payload) }, icon('share'), h('span', { text: 'Share' })),
        saveButton)));
}

/** Renders a long list a screenful at a time. */
function pagedList(items, renderItem, pageSize = 30) {
  const list = h('div', { class: 'passages' });
  const more = h('button', { type: 'button', class: 'btn btn-more', text: 'Show more' });
  let shown = 0;
  const showNext = () => {
    list.append(...items.slice(shown, shown + pageSize).map(renderItem));
    shown = Math.min(items.length, shown + pageSize);
    more.hidden = shown >= items.length;
  };
  more.addEventListener('click', showNext);
  showNext();
  return [list, more];
}

/* ---------- home ---------- */

function renderHome() {
  setTitle('');
  const translation = store.translation();
  const last = store.lastRead();

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

  const start = last
    ? h('a', { class: 'btn btn-primary', href: chapterPath(translation, last.book, last.chapter), text: `Continue reading ${reference(last.book, last.chapter)}` })
    : h('a', { class: 'btn btn-primary', href: chapterPath(translation, 1, 1), text: 'Start at Genesis 1' });

  const searchInput = h('input', { class: 'field', type: 'search', name: 'q', placeholder: 'A word or phrase', 'aria-label': 'Search the Bible', autocomplete: 'off' });
  const search = h('form', {
    class: 'home-search',
    role: 'search',
    onSubmit: (event) => {
      event.preventDefault();
      const query = searchInput.value.trim();
      if (query) navigate(`/search?q=${encodeURIComponent(query)}`);
      else searchInput.focus();
    },
  }, searchInput, h('button', { type: 'submit', class: 'btn', text: 'Search the Bible' }));

  $main.append(
    h('section', { class: 'hero' },
      h('h1', { class: 'sr-only', text: 'Scroll: read the Bible and find verses on the Fruits of the Spirit' }),
      verse,
      h('p', { class: 'hero-hint', text: 'Choose a word above to read verses about it.' })),
    h('section', { class: 'home-read', 'aria-label': 'Read the Bible' },
      h('div', { class: 'home-actions' },
        start,
        h('button', { type: 'button', class: 'btn', onClick: () => openPicker(), text: 'Choose a book' })),
      search),
    h('section', { class: 'home-app' },
      h('img', { src: '/img/logo.jpg', alt: '', width: '56', height: '56' }),
      h('p', null,
        'Scroll is also an iPhone and iPad app. Free, with no account and no ads. ',
        h('a', { href: APP_STORE_URL, rel: 'noopener', text: 'Get Scroll on the App Store' }), '.')));
}

/* ---------- reader ---------- */

function renderReader(route) {
  const { translation, book, chapter } = route;
  const title = reference(book, chapter);
  setTitle(`${title} ${translation}`);
  store.setLastRead(book, chapter);

  const previous = previousChapter(book, chapter);
  const next = nextChapter(book, chapter);
  const selected = new Set();
  let verses = [];

  const reader = h('article', { class: 'reader' });
  $main.append(reader);

  // The ribbon: saves the whole chapter.
  const ribbon = h('button', { type: 'button', class: 'ribbon' }, h('span', { class: 'ribbon-label' }));
  const saveChapterLabel = h('span');
  const saveChapterButton = h('button', { type: 'button', class: 'btn btn-quiet chapter-save' }, icon('bookmark'), saveChapterLabel);
  const paintRibbon = () => {
    const saved = store.isChapterSaved({ translation, book, chapter });
    const label = saved ? 'Chapter saved' : 'Save chapter';
    ribbon.setAttribute('aria-pressed', String(saved));
    ribbon.firstChild.textContent = label;
    saveChapterButton.setAttribute('aria-pressed', String(saved));
    saveChapterButton.classList.toggle('is-on', saved);
    saveChapterLabel.textContent = label;
  };
  const toggleChapterSaved = () => {
    toast(store.toggleChapter({ translation, book, chapter }) ? 'Chapter saved' : 'Removed from saved');
    paintRibbon();
  };
  ribbon.addEventListener('click', toggleChapterSaved);
  saveChapterButton.addEventListener('click', toggleChapterSaved);
  paintRibbon();

  const heading = h('h1', { class: 'chapter-title' }, bookName(book), h('span', { class: 'sr-only', text: ` ${chapter}` }));
  const text = h('div', { class: 'chapter-text' });

  const chapterLink = (target, direction) => {
    if (!target) return h('span');
    const label = reference(target.book, target.chapter);
    return h('a', { class: `chapter-step chapter-step-${direction}`, href: chapterPath(translation, target.book, target.chapter), rel: direction === 'next' ? 'next' : 'prev' },
      direction === 'previous' ? icon('left') : null,
      h('span', { text: label }),
      direction === 'next' ? icon('right') : null);
  };

  /* selection bar */
  const selectionLabel = h('span', { class: 'selection-ref' });
  const saveLabel = h('span');
  const saveButton = h('button', { type: 'button', class: 'btn btn-quiet' }, icon('bookmark'), saveLabel);
  const selectionBar = h('div', { class: 'selection', role: 'toolbar', 'aria-label': 'Selected verses', hidden: true },
    selectionLabel,
    h('div', { class: 'selection-actions' },
      h('button', { type: 'button', class: 'btn btn-quiet', onClick: () => shareVerses(selectionPayload()) }, icon('share'), h('span', { text: 'Share' })),
      h('button', { type: 'button', class: 'btn btn-quiet', onClick: () => copyVerses(selectionPayload()) }, icon('copy'), h('span', { text: 'Copy' })),
      saveButton,
      h('button', { type: 'button', class: 'btn btn-quiet btn-icon', 'aria-label': 'Clear selection', onClick: () => clearSelection() }, icon('close'))));
  document.body.append(selectionBar);
  onLeave(() => selectionBar.remove());

  const selectedVerses = () => verses.filter((v) => selected.has(v.verse));
  const selectionPayload = () => {
    const chosen = selectedVerses();
    return { text: chosen.map((v) => v.text).join(' '), translation, book, chapter, verses: chosen.map((v) => v.verse) };
  };
  const asSaved = (v) => ({ translation, book, chapter, verse: v.verse, text: v.text });
  const allSelectedSaved = () => selectedVerses().every((v) => store.isVerseSaved(asSaved(v)));

  const paintSelection = () => {
    text.querySelectorAll('.verse').forEach((node) => {
      const on = selected.has(Number(node.dataset.verse));
      node.classList.toggle('is-selected', on);
      node.querySelector('.verse-number').setAttribute('aria-pressed', String(on));
    });
    selectionBar.hidden = selected.size === 0;
    document.body.classList.toggle('has-selection', selected.size > 0);
    if (!selected.size) return;
    selectionLabel.textContent = reference(book, chapter, [...selected]);
    const saved = allSelectedSaved();
    saveLabel.textContent = saved ? 'Saved' : 'Save';
    saveButton.classList.toggle('is-on', saved);
    saveButton.setAttribute('aria-pressed', String(saved));
  };
  onLeave(() => document.body.classList.remove('has-selection'));

  const clearSelection = () => { selected.clear(); paintSelection(); };
  const toggleVerse = (number) => {
    if (selected.has(number)) selected.delete(number);
    else selected.add(number);
    paintSelection();
  };

  saveButton.addEventListener('click', () => {
    const chosen = selectedVerses();
    if (allSelectedSaved()) {
      chosen.forEach((v) => store.removeVerse(asSaved(v)));
      toast('Removed from saved');
    } else {
      chosen.forEach((v) => store.saveVerse(asSaved(v)));
      toast('Saved');
    }
    paintSelection();
  });

  text.addEventListener('click', (event) => {
    if (window.getSelection && String(window.getSelection())) return; // the reader is highlighting text to copy
    const node = event.target.closest('.verse');
    if (node) toggleVerse(Number(node.dataset.verse));
  });

  const onKey = (event) => {
    if (event.defaultPrevented || event.metaKey || event.ctrlKey || event.altKey) return;
    if (($picker && $picker.open) || event.target.closest('input, select, textarea, [contenteditable]')) return;
    if (event.key === 'Escape' && selected.size) clearSelection();
    else if (event.key === 'ArrowLeft' && previous) navigate(chapterPath(translation, previous.book, previous.chapter));
    else if (event.key === 'ArrowRight' && next) navigate(chapterPath(translation, next.book, next.chapter));
  };
  document.addEventListener('keydown', onKey);
  onLeave(() => document.removeEventListener('keydown', onKey));

  const signal = viewAbort.signal;
  const load = async () => {
    reader.replaceChildren(heading, loadingNote(`Loading ${title}`));
    try {
      verses = await fetchChapter(translation, book, chapter, signal);
    } catch (error) {
      if (signal.aborted) return;
      reader.replaceChildren(heading, errorNote(loadFailedMessage(title), load));
      return;
    }
    if (signal.aborted) return;

    const targets = new Set(route.verses.filter((number) => verses.some((v) => v.verse === number)));
    text.replaceChildren(
      h('span', { class: 'chapter-numeral', 'aria-hidden': 'true', text: String(chapter) }),
      ...verses.map((v, index) => h('span', { class: `verse${index === 0 ? ' verse-first' : ''}${targets.has(v.verse) ? ' is-target' : ''}`, id: `v${v.verse}`, 'data-verse': String(v.verse) },
        h('button', { type: 'button', class: 'verse-number', 'aria-pressed': 'false', 'aria-label': `Select verse ${v.verse}`, text: String(v.verse) }),
        `${v.text} `)));

    reader.replaceChildren(
      ribbon,
      heading,
      text,
      h('nav', { class: 'chapter-steps', 'aria-label': 'Chapters' }, chapterLink(previous, 'previous'), saveChapterButton, chapterLink(next, 'next')),
      h('p', { class: 'chapter-copyright', text: COPYRIGHT[translation] }));

    if (targets.size) {
      const first = document.getElementById(`v${Math.min(...targets)}`);
      if (first) first.scrollIntoView({ block: 'center' });
    }
    if (next) prefetchChapter(translation, next.book, next.chapter);
  };
  load();
}

/* ---------- Fruits of the Spirit ---------- */

function renderFruit({ fruit }) {
  const translation = store.translation();
  setTitle(`Verses about ${fruit.term}`);

  const results = h('div', { class: 'results' });
  const count = h('p', { class: 'page-lede' });
  let found = [];

  const show = () => {
    results.replaceChildren(...pagedList(found, (verse) => passage(verse)));
  };
  const shuffleButton = h('button', {
    type: 'button',
    class: 'btn',
    hidden: true,
    onClick: () => { found = shuffled(found); show(); },
  }, icon('shuffle'), h('span', { text: 'Shuffle' }));

  $main.append(
    h('header', { class: 'page-head' },
      h('h1', { class: 'page-title', text: fruit.name }),
      count,
      h('div', { class: 'page-tools' },
        shuffleButton,
        h('nav', { class: 'fruit-nav', 'aria-label': 'Fruits of the Spirit' },
          FRUITS.filter((item) => item.slug !== fruit.slug).map((item) => h('a', { href: `/fruit/${item.slug}`, text: item.name }))))),
    results);

  const signal = viewAbort.signal;
  const load = async () => {
    count.textContent = '';
    results.replaceChildren(loadingNote(`Finding verses about ${fruit.term}`));
    try {
      found = shuffled(await findAllVerses(translation, fruit.term, signal));
    } catch (error) {
      if (signal.aborted) return;
      results.replaceChildren(errorNote(loadFailedMessage('these verses'), load));
      return;
    }
    if (signal.aborted) return;
    if (!found.length) {
      results.replaceChildren(h('p', { class: 'status', text: `The ${translation} has no verses with the word “${fruit.term}”. Try another translation.` }));
      return;
    }
    count.textContent = `${found.length} verses in the ${translation} mention ${fruit.term}, in no particular order.`;
    shuffleButton.hidden = false;
    show();
  };
  load();
}

/* ---------- search ---------- */

function renderSearch({ query }) {
  const translation = store.translation();
  setTitle(query ? `“${query}”` : 'Search');

  const input = h('input', { class: 'field', type: 'search', name: 'q', value: query, placeholder: 'A word or phrase', 'aria-label': 'Search the Bible', autocomplete: 'off' });
  const form = h('form', {
    class: 'search-form',
    role: 'search',
    onSubmit: (event) => {
      event.preventDefault();
      const next = input.value.trim();
      if (!next) { input.focus(); return; }
      navigate(`/search?q=${encodeURIComponent(next)}`);
    },
  }, input, h('button', { type: 'submit', class: 'btn btn-primary', text: 'Search' }));

  const summary = h('p', { class: 'page-lede' });
  const results = h('div', { class: 'results' });
  $main.append(h('header', { class: 'page-head' }, h('h1', { class: 'page-title', text: 'Search' }), form, summary), results);

  if (!query) {
    input.focus();
    summary.textContent = `Search every verse in the ${translation}.`;
    return;
  }
  if (query.length < 2) {
    summary.textContent = 'Type at least two letters.';
    return;
  }

  const signal = viewAbort.signal;
  const list = h('div', { class: 'passages' });
  const more = h('button', { type: 'button', class: 'btn btn-more', text: 'Show more', hidden: true });
  let page = 0;
  let fetched = 0;
  let total = 0;

  const loadPage = async () => {
    more.disabled = true;
    const firstPage = page === 0;
    if (firstPage) results.replaceChildren(loadingNote(`Searching for “${query}”`));
    let data;
    try {
      data = await searchVerses(translation, query, page + 1, signal);
    } catch (error) {
      if (signal.aborted) return;
      if (firstPage) results.replaceChildren(errorNote(loadFailedMessage('the search'), loadPage));
      else { more.disabled = false; toast('Could not load more. Try again.'); }
      return;
    }
    if (signal.aborted) return;
    page += 1;
    fetched += data.fetched;
    total = data.total;
    if (firstPage) {
      if (!data.verses.length) {
        summary.textContent = '';
        results.replaceChildren(h('p', { class: 'status', text: `No verse in the ${translation} matches “${query}”. Check the spelling or try another translation.` }));
        return;
      }
      summary.textContent = `${total} ${total === 1 ? 'verse matches' : 'verses match'} “${query}” in the ${translation}.`;
      results.replaceChildren(list, more);
    }
    list.append(...data.verses.map((verse) => passage(verse)));
    more.disabled = false;
    more.hidden = data.fetched === 0 || fetched >= total;
  };
  more.addEventListener('click', loadPage);
  loadPage();
}

/* ---------- saved ---------- */

function renderSaved() {
  setTitle('Saved');
  const body = h('div', { class: 'saved-body' });
  $main.append(h('header', { class: 'page-head' }, h('h1', { class: 'page-title', text: 'Saved' })), body);

  const paint = () => {
    const verses = [...store.savedVerses()].reverse();
    const chapters = [...store.savedChapters()].reverse();

    if (!verses.length && !chapters.length) {
      body.replaceChildren(h('div', { class: 'status' },
        h('p', { text: 'Nothing saved yet. Tap a verse while reading and choose Save, or tap the ribbon at the top of a chapter to keep the whole chapter.' }),
        h('p', null, h('a', { class: 'btn', href: chapterPath(store.translation(), 43, 1), text: 'Open John 1' })),
        h('p', { class: 'status-note', text: 'Saved items stay in this browser on this device.' })));
      return;
    }

    const sections = [];
    if (chapters.length) {
      sections.push(h('section', { class: 'saved-section' },
        h('h2', { class: 'section-title', text: 'Chapters' }),
        h('ul', { class: 'saved-chapters' }, chapters.map((item) => h('li', null,
          h('a', { class: 'saved-chapter', href: chapterPath(resolveTranslation(item.translation), item.book, item.chapter) },
            h('span', { class: 'saved-chapter-name', text: reference(item.book, item.chapter) }),
            h('span', { class: 'passage-translation', text: resolveTranslation(item.translation) })),
          h('button', {
            type: 'button',
            class: 'btn btn-quiet',
            'aria-label': `Remove ${reference(item.book, item.chapter)}`,
            onClick: () => { store.removeChapter(item); toast('Removed from saved'); paint(); },
          }, h('span', { text: 'Remove' })))))));
    }
    if (verses.length) {
      sections.push(h('section', { class: 'saved-section' },
        h('h2', { class: 'section-title', text: 'Verses' }),
        h('div', { class: 'passages' }, verses.map((verse) => passage(verse, { removable: true, onRemoved: paint })))));
    }
    sections.push(h('p', { class: 'status-note', text: 'Saved items stay in this browser on this device.' }));
    body.replaceChildren(...sections);
  };
  paint();
}

/* ---------- about, privacy, not found ---------- */

function prose(title, ...blocks) {
  $main.append(h('article', { class: 'prose' }, h('h1', { class: 'page-title', text: title }), ...blocks));
}

function renderAbout() {
  setTitle('About');
  prose('About Scroll',
    h('p', { text: 'Scroll is a quiet place to read the Bible. Open any chapter, pick the translation you like, save what you want to come back to, and share a verse with a link that opens right on it.' }),
    h('p', null,
      'It began as an iPhone app built around the Fruits of the Spirit in Galatians 5: love, joy, peace, patience, kindness, goodness, faithfulness, gentleness and self-control. The home page still starts there. ',
      h('a', { href: APP_STORE_URL, rel: 'noopener', text: 'Scroll is free on the App Store' }), '.'),
    h('p', { text: 'There is no account, no ads and no tracking. Anything you save stays on your device.' }),
    h('h2', { text: 'Translations' }),
    ...TRANSLATIONS.map((t) => h('p', { class: 'prose-small' }, h('strong', { text: `${t.name} (${t.code}). ` }), COPYRIGHT[t.code])),
    h('p', { class: 'prose-small' }, 'Bible text is loaded from ', h('a', { href: 'https://bolls.life', rel: 'noopener', text: 'bolls.life' }), '.'),
    h('h2', { text: 'Contact' }),
    h('p', null, 'Questions or a problem with the site? Email ', h('a', { href: `mailto:${CONTACT_EMAIL}`, text: CONTACT_EMAIL }), '.'));
}

function renderPrivacy() {
  setTitle('Privacy');
  prose('Privacy',
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

function renderNotFound() {
  setTitle('Page not found');
  prose('That page is not here',
    h('p', { text: 'The address may be mistyped, or it points to a book or chapter that does not exist.' }),
    h('p', null,
      h('a', { class: 'btn btn-primary', href: '/', text: 'Go to the home page' }), ' ',
      h('button', { type: 'button', class: 'btn', onClick: () => openPicker(), text: 'Choose a book' })));
}

/* ---------- footer and start ---------- */

function buildFooter() {
  $foot.replaceChildren(
    h('nav', { class: 'foot-links', 'aria-label': 'Site' },
      h('a', { href: '/about', text: 'About' }),
      h('a', { href: '/privacy', text: 'Privacy' }),
      h('a', { href: APP_STORE_URL, rel: 'noopener', text: 'iPhone app' })),
    h('p', { class: 'foot-note', text: `© ${new Date().getFullYear()} Okeke LLC` }));
}

applyDisplay();
buildBar();
buildFooter();
render();
