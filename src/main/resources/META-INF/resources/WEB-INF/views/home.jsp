<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8"/>
  <title>Finance App — Home</title>
  <meta name="viewport" content="width=device-width, initial-scale=1"/>
  <style>
    :root { --br:12px; }
    * { box-sizing: border-box; }
    body { font-family: system-ui, -apple-system, Segoe UI, Roboto, sans-serif; margin: 24px; color:#111; }
    h1 { margin: 0 0 6px; }
    .muted { color:#666; font-size: 0.95rem; }
    .wrap { display:grid; gap:16px; max-width:1000px; }
    .card { border:1px solid #e6e6e6; border-radius: var(--br); padding:16px; background:#fff; }
    .row { display:flex; gap:12px; align-items:center; flex-wrap:wrap; }
    input, button, select { padding:10px 12px; border-radius:10px; border:1px solid #ccc; font-size:14px; }
    input { min-width:200px; }
    button { cursor:pointer; }
    pre { background:#fafafa; border:1px solid #eee; border-radius:10px; padding:12px; overflow:auto; max-height:320px; }
    .badge { display:inline-block; padding:2px 8px; border-radius:999px; border:1px solid #bbb; font-size:12px; }
    .hr { height:1px; background:#eee; margin:12px 0; }
  </style>
</head>
<body>
  <h1>Finance App</h1>
  <div class="muted">Quick test console for auth and protected endpoints.</div>

  <div class="wrap">

    <!-- JWT tools -->
    <div class="card">
      <div class="row" style="justify-content:space-between;">
        <div class="row">
          <span class="badge">JWT</span>
          <button id="btnShowJwt">Show Stored JWT</button>
          <button id="btnClearJwt">Clear JWT</button>
        </div>
        <small class="muted">Stored in <code>localStorage</code></small>
      </div>
      <pre id="jwtBox">(no token)</pre>
    </div>

    <!-- Login -->
    <div class="card">
      <h2 style="margin:0 0 8px;">Login</h2>
      <div class="row">
        <input id="email" type="email" placeholder="email@example.com" />
        <input id="password" type="password" placeholder="password" />
        <button id="btnLogin">POST /auth/login → store JWT</button>
      </div>
      <div class="muted" style="margin-top:6px">
        Expects your <code>/auth/login</code> to return something like <code>{"token":"&lt;jwt&gt;"}</code>.
      </div>
      <pre id="loginResult">awaiting…</pre>
    </div>

    <!-- Register (optional) -->
    <div class="card">
      <h2 style="margin:0 0 8px;">Register (Optional)</h2>
      <div class="row">
        <input id="regName" type="text" placeholder="name" />
        <input id="regEmail" type="email" placeholder="email@example.com" />
        <input id="regPassword" type="password" placeholder="password" />
        <button id="btnRegister">POST /auth/register</button>
      </div>
      <pre id="registerResult">awaiting…</pre>
    </div>

    <!-- Protected API probes -->
    <div class="card">
      <h2 style="margin:0 0 8px;">Protected calls</h2>
      <div class="row" style="gap:8px;">
        <button data-path="/accounts" class="hit">GET /accounts</button>
        <button data-path="/transactions" class="hit">GET /transactions</button>
        <button data-path="/users/me" class="hit">GET /users/me</button>
        <div class="hr"></div>
        <button id="btnHealth">GET /health (no auth)</button>
      </div>
      <pre id="apiResult">awaiting…</pre>
    </div>

  </div>

  <script>
    // --- tiny util ---
    const $ = sel => document.querySelector(sel);
    const jwtBox = $('#jwtBox');
    const loginResult = $('#loginResult');
    const registerResult = $('#registerResult');
    const apiResult = $('#apiResult');

    const getToken = () => localStorage.getItem('jwt') || '';
    const setToken = t => { localStorage.setItem('jwt', t); renderJwt(); };
    const clearToken = () => { localStorage.removeItem('jwt'); renderJwt(); };
    const renderJwt = () => { jwtBox.textContent = getToken() || '(no token)'; };
    renderJwt();

    $('#btnShowJwt').onclick = renderJwt;
    $('#btnClearJwt').onclick = clearToken;

    // --- login ---
    $('#btnLogin').onclick = async () => {
      const email = $('#email').value.trim();
      const password = $('#password').value;

      try {
        const res = await fetch('/auth/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email, password })
        });
        const data = await res.json();
        loginResult.textContent = JSON.stringify(data, null, 2);

        // If your backend returns { jwt: "..." } or { accessToken: "..." }, change the key here:
        const token = data.token || data.jwt || data.accessToken;
        if (token) setToken(token);
      } catch (e) {
        loginResult.textContent = e.toString();
      }
    };

    // --- register (optional) ---
    $('#btnRegister').onclick = async () => {
      const name = $('#regName').value.trim();
      const email = $('#regEmail').value.trim();
      const password = $('#regPassword').value;

      try {
        const res = await fetch('/auth/register', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ name, email, password })
        });
        const data = await res.json();
        registerResult.textContent = JSON.stringify(data, null, 2);
      } catch (e) {
        registerResult.textContent = e.toString();
      }
    };

    // --- generic GET with bearer ---
    document.querySelectorAll('.hit').forEach(btn => {
      btn.onclick = async () => {
        apiResult.textContent = 'loading…';
        const token = getToken();
        try {
          const res = await fetch(btn.dataset.path, {
            headers: token ? { 'Authorization': 'Bearer ' + token } : {}
          });
          const text = await res.text();
          try {
            apiResult.textContent = JSON.stringify(JSON.parse(text), null, 2);
          } catch {
            apiResult.textContent = text;
          }
        } catch (e) {
          apiResult.textContent = e.toString();
        }
      };
    });

    // --- health (public) ---
    $('#btnHealth').onclick = async () => {
      try {
        const res = await fetch('/health');
        const data = await res.json();
        apiResult.textContent = JSON.stringify(data, null, 2);
      } catch (e) {
        apiResult.textContent = e.toString();
      }
    };
  </script>
</body>
</html>
