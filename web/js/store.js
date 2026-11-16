// Everything the site remembers lives in this browser's localStorage. Nothing
// is sent anywhere. Every access is wrapped because storage can be blocked
// (private windows, strict settings); the site still works, it just forgets.

import { DEFAULT_TRANSLATION, resolveTranslation } from './bible.js';

const KEYS = {
  verses: 'scroll.savedVerses',
  chapters: 'scroll.savedChapters',
  translation: 'scroll.translation',
  last: 'scroll.lastRead',
  theme: 'scroll.theme',
  textSize: 'scroll.textSize',
};

function read(key, fallback) {
  try {
    const raw = localStorage.getItem(key);
    return raw === null ? fallback : JSON.parse(raw);
  } catch {
    return fallback;
  }
}

function write(key, value) {
  try {
    localStorage.setItem(key, JSON.stringify(value));
  } catch {
    /* storage unavailable: keep going without persistence */
  }
  window.dispatchEvent(new CustomEvent('scroll:store', { detail: { key } }));
}

const verseKey = (v) => `${v.translation}-${v.book}-${v.chapter}-${v.verse}`;
const chapterKey = (c) => `${c.translation}-${c.book}-${c.chapter}`;

export const store = {
  // Saved verses: [{ translation, book, chapter, verse, text, savedAt }]
  savedVerses() {
    const list = read(KEYS.verses, []);
    return Array.isArray(list) ? list : [];
  },
  isVerseSaved(v) {
    return this.savedVerses().some((saved) => verseKey(saved) === verseKey(v));
  },
  saveVerse(v) {
    if (this.isVerseSaved(v)) return;
    const { translation, book, chapter, verse, text } = v;
    write(KEYS.verses, [...this.savedVerses(), { translation, book, chapter, verse, text, savedAt: Date.now() }]);
  },
  removeVerse(v) {
    write(KEYS.verses, this.savedVerses().filter((saved) => verseKey(saved) !== verseKey(v)));
  },

  // Saved chapters: [{ translation, book, chapter, savedAt }]
  savedChapters() {
    const list = read(KEYS.chapters, []);
    return Array.isArray(list) ? list : [];
  },
  isChapterSaved(c) {
    return this.savedChapters().some((saved) => chapterKey(saved) === chapterKey(c));
  },
  toggleChapter(c) {
    if (this.isChapterSaved(c)) {
      write(KEYS.chapters, this.savedChapters().filter((saved) => chapterKey(saved) !== chapterKey(c)));
      return false;
    }
    const { translation, book, chapter } = c;
    write(KEYS.chapters, [...this.savedChapters(), { translation, book, chapter, savedAt: Date.now() }]);
    return true;
  },
  removeChapter(c) {
    write(KEYS.chapters, this.savedChapters().filter((saved) => chapterKey(saved) !== chapterKey(c)));
  },

  savedCount() {
    return this.savedVerses().length + this.savedChapters().length;
  },

  translation() {
    return resolveTranslation(read(KEYS.translation, DEFAULT_TRANSLATION));
  },
  setTranslation(code) {
    write(KEYS.translation, resolveTranslation(code));
  },

  lastRead() {
    const last = read(KEYS.last, null);
    return last && Number.isInteger(last.book) && Number.isInteger(last.chapter) ? last : null;
  },
  setLastRead(book, chapter) {
    write(KEYS.last, { book, chapter });
  },

  theme() {
    const theme = read(KEYS.theme, 'system');
    return ['system', 'light', 'dark'].includes(theme) ? theme : 'system';
  },
  setTheme(theme) {
    write(KEYS.theme, theme);
  },

  textSize() {
    const size = read(KEYS.textSize, 1);
    return [0, 1, 2, 3].includes(size) ? size : 1;
  },
  setTextSize(size) {
    write(KEYS.textSize, size);
  },
};
