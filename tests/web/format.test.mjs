// Run with: node --test tests/web/format.test.mjs
import test from 'node:test';
import assert from 'node:assert/strict';
import {
  cleanVerseText, formatVerseList, parseVerseSpec, reference, versePath, chapterPath, shareParts,
} from '../../web/js/format.js';
import {
  nextChapter, previousChapter, resolveTranslation, isValidLocation, BOOKS, CHAPTER_COUNTS, FRUITS,
} from '../../web/js/bible.js';

test('cleans KJV Strong\'s numbers and translator notes', () => {
  const raw = 'He maketh me to lie down<S>7257</S> in green<S>1877</S> pastures<S>4999</S>: he leadeth<S>5095</S> me beside the still<S>4496</S> waters<S>4325</S>. <sup>green: Heb. pastures of tender grass</sup> <sup>still: Heb. waters of quietness</sup>';
  assert.equal(cleanVerseText(raw), 'He maketh me to lie down in green pastures: he leadeth me beside the still waters.');
});

test('turns line breaks into spaces (NLT poetry)', () => {
  assert.equal(cleanVerseText('The LORD is my shepherd;<br>I have all that I need.<br/>'), 'The LORD is my shepherd; I have all that I need.');
});

test('trims and collapses spaces (ESV)', () => {
  assert.equal(cleanVerseText(' “For God so loved the world,  that he gave his only Son'), '“For God so loved the world, that he gave his only Son');
});

test('keeps the text inside search highlights', () => {
  assert.equal(cleanVerseText('Be<mark>love</mark>d, let us <mark>love</mark> one another'), 'Beloved, let us love one another');
});

test('handles missing text', () => {
  assert.equal(cleanVerseText(undefined), '');
  assert.equal(cleanVerseText(null), '');
});

test('formats verse lists', () => {
  assert.equal(formatVerseList([16]), '16');
  assert.equal(formatVerseList([18, 16, 17]), '16-18');
  assert.equal(formatVerseList([16, 17, 18, 20, 22, 23]), '16-18, 20, 22-23');
  assert.equal(formatVerseList([3, 3]), '3');
});

test('parses verse specs from the address', () => {
  assert.deepEqual(parseVerseSpec('16'), [16]);
  assert.deepEqual(parseVerseSpec('22-23'), [22, 23]);
  assert.deepEqual(parseVerseSpec('23-22'), []);
  assert.deepEqual(parseVerseSpec('abc'), []);
  assert.deepEqual(parseVerseSpec(undefined), []);
  assert.deepEqual(parseVerseSpec('0'), []);
});

test('builds references and paths', () => {
  assert.equal(reference(43, 3), 'John 3');
  assert.equal(reference(43, 3, [16]), 'John 3:16');
  assert.equal(reference(48, 5, [22, 23]), 'Galatians 5:22-23');
  assert.equal(chapterPath('ESV', 43, 3), '/ESV/43/3/');
  assert.equal(versePath('ESV', 43, 3, [16]), '/ESV/43/3/16');
  assert.equal(versePath('ESV', 43, 3, [17, 16]), '/ESV/43/3/16-17');
  assert.equal(versePath('ESV', 43, 3, [16, 18]), '/ESV/43/3/16');
  assert.equal(versePath('ESV', 43, 3, []), '/ESV/43/3/');
});

test('share text matches the apps', () => {
  const parts = shareParts({ text: 'Jesus wept.', translation: 'ESV', book: 43, chapter: 11, verses: [35] });
  assert.equal(parts.full, 'Jesus wept.\n\nJohn 11:35 (ESV)\nhttps://scroll.okeke.us/ESV/43/11/35');
  assert.equal(parts.body, 'Jesus wept.\n\nJohn 11:35 (ESV)');
  assert.equal(parts.url, 'https://scroll.okeke.us/ESV/43/11/35');
});

test('walks chapters across book and Bible boundaries', () => {
  assert.deepEqual(nextChapter(1, 1), { book: 1, chapter: 2 });
  assert.deepEqual(nextChapter(1, 50), { book: 2, chapter: 1 });
  assert.equal(nextChapter(66, 22), null);
  assert.deepEqual(previousChapter(2, 1), { book: 1, chapter: 50 });
  assert.deepEqual(previousChapter(40, 1), { book: 39, chapter: 4 });
  assert.equal(previousChapter(1, 1), null);
});

test('book data is complete', () => {
  assert.equal(BOOKS.length, 66);
  assert.equal(CHAPTER_COUNTS.length, 66);
  assert.equal(CHAPTER_COUNTS.reduce((a, b) => a + b, 0), 1189);
  assert.equal(FRUITS.length, 9);
});

test('unknown translations fall back to ESV', () => {
  assert.equal(resolveTranslation('NIV'), 'ESV');
  assert.equal(resolveTranslation('ylt'), 'ESV');
  assert.equal(resolveTranslation('kjv'), 'KJV');
  assert.equal(resolveTranslation(undefined), 'ESV');
});

test('rejects chapters that do not exist', () => {
  assert.equal(isValidLocation(43, 21), true);
  assert.equal(isValidLocation(43, 22), false);
  assert.equal(isValidLocation(67, 1), false);
  assert.equal(isValidLocation(0, 1), false);
});
