const buttons  = document.querySelectorAll('.nav-btn');
const sections = document.querySelectorAll('.main-card');

buttons.forEach(btn => {
  btn.addEventListener('click', () => {
    // active button
    buttons.forEach(b => b.classList.remove('active'));
    btn.classList.add('active');

    // show only the matching section
    const cat = btn.dataset.cat;
    sections.forEach(sec => {
      sec.classList.toggle('hidden', sec.dataset.cat !== cat);
    });
  });
});
