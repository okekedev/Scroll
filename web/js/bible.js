// Books, translations and the Fruits of the Spirit. Book numbers (1 = Genesis …
// 66 = Revelation) match bolls.life and the iPhone and Android apps.

export const TRANSLATIONS = [
  { code: 'ESV', name: 'English Standard Version' },
  { code: 'KJV', name: 'King James Version' },
  { code: 'NASB', name: 'New American Standard Bible' },
  { code: 'NLT', name: 'New Living Translation' },
];

export const DEFAULT_TRANSLATION = 'ESV';

export function isTranslation(code) {
  return TRANSLATIONS.some((t) => t.code === code);
}

/** Maps any translation code onto one the site offers (NIV, YLT … → ESV). */
export function resolveTranslation(code) {
  const upper = String(code || '').toUpperCase();
  return isTranslation(upper) ? upper : DEFAULT_TRANSLATION;
}

export const BOOKS = [
  'Genesis', 'Exodus', 'Leviticus', 'Numbers', 'Deuteronomy', 'Joshua', 'Judges', 'Ruth',
  '1 Samuel', '2 Samuel', '1 Kings', '2 Kings', '1 Chronicles', '2 Chronicles', 'Ezra',
  'Nehemiah', 'Esther', 'Job', 'Psalms', 'Proverbs', 'Ecclesiastes', 'Song of Solomon',
  'Isaiah', 'Jeremiah', 'Lamentations', 'Ezekiel', 'Daniel', 'Hosea', 'Joel', 'Amos',
  'Obadiah', 'Jonah', 'Micah', 'Nahum', 'Habakkuk', 'Zephaniah', 'Haggai', 'Zechariah',
  'Malachi', 'Matthew', 'Mark', 'Luke', 'John', 'Acts', 'Romans', '1 Corinthians',
  '2 Corinthians', 'Galatians', 'Ephesians', 'Philippians', 'Colossians', '1 Thessalonians',
  '2 Thessalonians', '1 Timothy', '2 Timothy', 'Titus', 'Philemon', 'Hebrews', 'James',
  '1 Peter', '2 Peter', '1 John', '2 John', '3 John', 'Jude', 'Revelation',
];

export const CHAPTER_COUNTS = [
  50, 40, 27, 36, 34, 24, 21, 4, 31, 24, 22, 25, 29, 36, 10, 13, 10, 42, 150, 31,
  12, 8, 66, 52, 5, 48, 12, 14, 3, 9, 1, 4, 7, 3, 3, 3, 2, 14, 4,
  28, 16, 24, 21, 28,
  16, 16, 13, 6, 6, 4, 4, 5, 3, 6, 4, 3, 1, 13, 5, 5, 3, 5, 1, 1, 1, 22,
];

export const OLD_TESTAMENT_BOOKS = 39;

export function bookName(book) {
  return BOOKS[book - 1] || '';
}

export function chaptersIn(book) {
  return CHAPTER_COUNTS[book - 1] || 0;
}

export function isValidLocation(book, chapter) {
  return Number.isInteger(book) && Number.isInteger(chapter)
    && book >= 1 && book <= BOOKS.length
    && chapter >= 1 && chapter <= chaptersIn(book);
}

export function nextChapter(book, chapter) {
  if (chapter < chaptersIn(book)) return { book, chapter: chapter + 1 };
  if (book < BOOKS.length) return { book: book + 1, chapter: 1 };
  return null;
}

export function previousChapter(book, chapter) {
  if (chapter > 1) return { book, chapter: chapter - 1 };
  if (book > 1) return { book: book - 1, chapter: chaptersIn(book - 1) };
  return null;
}

// Galatians 5:22-23. `word` is how the virtue reads inside the verse on the
// home page; `term` is what the verse search looks for.
export const FRUITS = [
  { slug: 'love', name: 'Love', word: 'love', term: 'love' },
  { slug: 'joy', name: 'Joy', word: 'joy', term: 'joy' },
  { slug: 'peace', name: 'Peace', word: 'peace', term: 'peace' },
  { slug: 'patience', name: 'Patience', word: 'patience', term: 'patience' },
  { slug: 'kindness', name: 'Kindness', word: 'kindness', term: 'kindness' },
  { slug: 'goodness', name: 'Goodness', word: 'goodness', term: 'goodness' },
  { slug: 'faith', name: 'Faith', word: 'faithfulness', term: 'faith' },
  { slug: 'gentleness', name: 'Gentleness', word: 'gentleness', term: 'gentleness' },
  { slug: 'self-control', name: 'Self-Control', word: 'self-control', term: 'self-control' },
];

export const COPYRIGHT = {
  ESV: 'Scripture quotations are from the ESV® Bible (The Holy Bible, English Standard Version®), © 2001 by Crossway, a publishing ministry of Good News Publishers. Used by permission. All rights reserved.',
  KJV: 'King James Version. Public domain.',
  NASB: 'Scripture quotations taken from the (NASB®) New American Standard Bible®, Copyright © 1960, 1971, 1977, 1995 by The Lockman Foundation. Used by permission. All rights reserved. lockman.org',
  NLT: 'Scripture quotations are taken from the Holy Bible, New Living Translation, copyright © 1996, 2004, 2015 by Tyndale House Foundation. Used by permission of Tyndale House Publishers, Carol Stream, Illinois 60188. All rights reserved.',
};
