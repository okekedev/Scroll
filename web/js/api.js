import { BOOKS } from './bible.js';
import { cleanVerseText } from './format.js';

const API = 'https://bolls.life';
const chapterCache = new Map();

async function getJson(url, signal) {
  const response = await fetch(url, { signal });
  if (!response.ok) throw new Error(`Request failed (${response.status})`);
  return response.json();
}

/** → [{ verse, text }] for one chapter. Cached for the life of the page. */
export async function fetchChapter(translation, book, chapter, signal) {
  const key = `${translation}/${book}/${chapter}`;
  if (chapterCache.has(key)) return chapterCache.get(key);
  const rows = await getJson(`${API}/get-chapter/${translation}/${book}/${chapter}/`, signal);
  if (!Array.isArray(rows) || !rows.length) throw new Error('Chapter came back empty');
  const verses = rows
    .map((row) => ({ verse: Number(row.verse), text: cleanVerseText(row.text) }))
    .filter((row) => row.verse > 0 && row.text);
  chapterCache.set(key, verses);
  return verses;
}

/** Warm the cache for a chapter the reader is likely to open next. */
export function prefetchChapter(translation, book, chapter) {
  fetchChapter(translation, book, chapter).catch(() => {});
}

function toVerse(row, translation) {
  return {
    translation,
    book: Number(row.book),
    chapter: Number(row.chapter),
    verse: Number(row.verse),
    text: cleanVerseText(row.text),
  };
}

// Some translations (KJV) also return Apocrypha; the site covers the 66 books.
const inCanon = (verse) => verse.book >= 1 && verse.book <= BOOKS.length && verse.text;

/** Every verse matching a term (the endpoint ignores its limit). Used for the Fruits of the Spirit pages. */
export async function findAllVerses(translation, term, signal) {
  const url = `${API}/find/${translation}/?search=${encodeURIComponent(term)}&match_case=false&match_whole=false&limit=20`;
  const rows = await getJson(url, signal);
  if (!Array.isArray(rows)) throw new Error('Unexpected search response');
  return rows.map((row) => toVerse(row, translation)).filter(inCanon);
}

/** One page of search results → { verses, total }. */
export async function searchVerses(translation, query, page, signal, pageSize = 40) {
  const url = `${API}/v2/find/${translation}?search=${encodeURIComponent(query)}&match_case=false&match_whole=false&limit=${pageSize}&page=${page}`;
  const data = await getJson(url, signal);
  const rows = Array.isArray(data?.results) ? data.results : [];
  return {
    verses: rows.map((row) => toVerse(row, translation)).filter(inCanon),
    total: Number(data?.total) || rows.length,
    fetched: rows.length,
  };
}
