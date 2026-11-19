// Google Analytics for scroll.okeke.us (property "Scroll").
// Counts visits to the website and which pages are read. The apps do not use it.
// Moving between pages inside the site is counted by Google from the address changes.
// It lives in its own file because the site's security policy allows no inline scripts.
(function () {
  var id = 'G-17X7TSSSDB';
  var tag = document.createElement('script');
  tag.async = true;
  tag.src = 'https://www.googletagmanager.com/gtag/js?id=' + id;
  document.head.appendChild(tag);

  window.dataLayer = window.dataLayer || [];
  function gtag() { window.dataLayer.push(arguments); }
  window.gtag = gtag;
  gtag('js', new Date());
  gtag('config', id);
})();
