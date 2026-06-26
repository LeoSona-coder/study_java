(() => {
  const current = location.pathname.split('/').pop() || 'index.html';
  document.querySelectorAll('.toc a').forEach((link) => {
    const href = link.getAttribute('href') || '';
    if (href.endsWith(current)) link.classList.add('active');
  });

  document.querySelectorAll('pre code').forEach((block) => {
    block.setAttribute('tabindex', '0');
  });
})();
