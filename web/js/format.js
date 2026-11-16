import { bookName } from './bible.js';

export const SITE_ORIGIN = 'https://scroll.okeke.us';

/**
 * bolls.life returns verse text with light HTML: <br> line breaks, <mark>
 * search highlights, and in the KJV Strong's numbers (<S>1063</S>) and
 * translator notes (<sup>…</sup>). Numbers and notes are dropped whole; every
 * other tag is removed and its text kept. Same rules as the iPhone and Android apps.
 */
export function cleanVerseText(raw) {
  return String(raw ?? '')
    .replace(/<S>[^<]*<\/S>/g, '')
    .replace(/<sup>.*?<\/sup>/g, '')
    .replace(/<br\s*\/?>/g, ' ')
    .replace(/<[^>]+>/g, '')
    .replace(/\s+/g, ' ')
    .trim();
}

/** [16, 17, 18, 20] → "16-18, 20" */
export function formatVerseList(verses) {
  const sorted = [...new Set(verses)].sort((a, b) => a - b);
  const parts = [];
  for (let i = 0; i < sorted.length; i += 1) {
    let end = i;
    while (end + 1 < sorted.length && sorted[end + 1] === sorted[end] + 1) end += 1;
    parts.push(end > i ? `${sorted[i]}-${sorted[end]}` : `${sorted[i]}`);
    i = end;
  }
  return parts.join(', ');
}

/** "16-18" or "16" → [16, 17, 18]; anything unreadable → []. */
export function parseVerseSpec(spec) {
  const match = /^(\d{1,3})(?:-(\d{1,3}))?$/.exec(String(spec || ''));
  if (!match) return [];
  const start = Number(match[1]);
  const end = match[2] ? Number(match[2]) : start;
  if (start < 1 || end < start || end - start > 200) return [];
  return Array.from({ length: end - start + 1 }, (_, i) => start + i);
}

export function reference(book, chapter, verses = []) {
  const base = `${bookName(book)} ${chapter}`;
  return verses.length ? `${base}:${formatVerseList(verses)}` : base;
}

export function chapterPath(translation, book, chapter) {
  return `/${translation}/${book}/${chapter}/`;
}

/** Path to a verse or a run of verses. A scattered selection links to its first verse. */
export function versePath(translation, book, chapter, verses) {
  const sorted = [...new Set(verses)].sort((a, b) => a - b);
  if (!sorted.length) return chapterPath(translation, book, chapter);
  const contiguous = sorted[sorted.length - 1] - sorted[0] === sorted.length - 1;
  const spec = contiguous && sorted.length > 1 ? `${sorted[0]}-${sorted[sorted.length - 1]}` : `${sorted[0]}`;
  return `/${translation}/${book}/${chapter}/${spec}`;
}

/**
 * What gets shared or copied. Matches the apps:
 *   <text>
 *
 *   John 3:16 (ESV)
 *   https://scroll.okeke.us/ESV/43/3/16
 */
export function shareParts({ text, translation, book, chapter, verses }) {
  const body = `${text}\n\n${reference(book, chapter, verses)} (${translation})`;
  const url = SITE_ORIGIN + versePath(translation, book, chapter, verses);
  return { body, url, full: `${body}\n${url}` };
}
