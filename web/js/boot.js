// Runs before first paint (plain script in <head>) so the saved theme and text
// size apply without a flash. Mirrors the keys in store.js.
(function () {
  var root = document.documentElement;
  try {
    var theme = JSON.parse(localStorage.getItem('scroll.theme') || '"system"');
    if (theme === 'light' || theme === 'dark') root.setAttribute('data-theme', theme);
    var size = JSON.parse(localStorage.getItem('scroll.textSize') || '1');
    if (size === 0 || size === 2 || size === 3) root.setAttribute('data-text-size', String(size));
  } catch (e) {
    /* storage blocked: defaults apply */
  }
})();
