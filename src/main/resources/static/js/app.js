(function () {
  const body = document.body;
  const toggle = document.getElementById('themeToggle');
  const icon = document.getElementById('themeIcon');
  const label = document.getElementById('themeLabel');
  function applyTheme(theme) {
    body.classList.toggle('dark-theme', theme === 'dark');
    if (icon) icon.textContent = theme === 'dark' ? '☀' : '☾';
    if (label) label.textContent = theme === 'dark' ? 'Light theme' : 'Dark theme';
  }
  let saved = 'light';
  try { saved = localStorage.getItem('premortem-workspace-theme') || 'light'; } catch (e) {}
  applyTheme(saved);
  if (toggle) toggle.addEventListener('click', function () {
    const next = body.classList.contains('dark-theme') ? 'light' : 'dark';
    applyTheme(next);
    try { localStorage.setItem('premortem-workspace-theme', next); } catch (e) {}
  });
})();
