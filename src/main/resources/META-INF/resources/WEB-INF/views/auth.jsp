<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login / Sign Up - FinanceTracker</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }

        .auth-container {
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            width: 100%;
            max-width: 450px;
            overflow: hidden;
        }

        .auth-header {
            text-align: center;
            padding: 40px 40px 20px;
        }

        .auth-header h1 {
            font-size: 32px;
            color: #333;
            margin-bottom: 10px;
        }

        .auth-header p {
            color: #666;
            font-size: 16px;
        }

        .tabs {
            display: flex;
            border-bottom: 2px solid #e0e0e0;
        }

        .tab {
            flex: 1;
            padding: 15px;
            text-align: center;
            background: none;
            border: none;
            font-size: 16px;
            font-weight: 600;
            color: #999;
            cursor: pointer;
            transition: all 0.3s;
            position: relative;
        }

        .tab:hover {
            color: #667eea;
        }

        .tab.active {
            color: #667eea;
        }

        .tab.active::after {
            content: '';
            position: absolute;
            bottom: -2px;
            left: 0;
            right: 0;
            height: 2px;
            background: #667eea;
        }

        .tab-content {
            display: none;
            padding: 40px;
        }

        .tab-content.active {
            display: block;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #333;
            font-weight: 500;
            font-size: 14px;
        }

        .form-group input {
            width: 100%;
            padding: 12px 15px;
            border: 2px solid #e0e0e0;
            border-radius: 10px;
            font-size: 15px;
            transition: border-color 0.3s;
        }

        .form-group input:focus {
            outline: none;
            border-color: #667eea;
        }

        .btn {
            width: 100%;
            padding: 14px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 10px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: transform 0.2s, box-shadow 0.2s;
        }

        .btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
        }

        .btn:active {
            transform: translateY(0);
        }

        .error-message {
            background: #fee;
            color: #c33;
            padding: 12px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 14px;
            display: none;
        }

        .success-message {
            background: #efe;
            color: #3c3;
            padding: 12px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 14px;
            display: none;
        }

        .back-link {
            text-align: center;
            margin-top: 20px;
        }

        .back-link a {
            color: #667eea;
            text-decoration: none;
            font-weight: 500;
        }

        .back-link a:hover {
            text-decoration: underline;
        }

        .password-requirements {
            font-size: 12px;
            color: #999;
            margin-top: 5px;
        }

        @media (max-width: 480px) {
            .auth-container {
                border-radius: 0;
            }

            .tab-content {
                padding: 30px 20px;
            }
        }
    </style>
</head>
<body>
    <div class="auth-container">
        <div class="auth-header">
            <h1>💰 FinanceTracker</h1>
            <p>Manage your finances smarter</p>
        </div>

        <div class="tabs">
            <button class="tab active" onclick="switchTab('login')">Login</button>
            <button class="tab" onclick="switchTab('register')">Sign Up</button>
        </div>

        <!-- Login Tab -->
        <div id="login-tab" class="tab-content active">
            <div id="login-error" class="error-message"></div>
            <div id="login-success" class="success-message"></div>

            <form id="login-form" onsubmit="handleLogin(event)">
                <div class="form-group">
                    <label for="login-email">Email Address</label>
                    <input type="email" id="login-email" required placeholder="your@email.com">
                </div>

                <div class="form-group">
                    <label for="login-password">Password</label>
                    <input type="password" id="login-password" required placeholder="Enter your password">
                </div>

                <button type="submit" class="btn">Login</button>
            </form>

            <div class="back-link">
                <a href="/">← Back to Home</a>
            </div>
        </div>

        <!-- Register Tab -->
        <div id="register-tab" class="tab-content">
            <div id="register-error" class="error-message"></div>
            <div id="register-success" class="success-message"></div>

            <form id="register-form" onsubmit="handleRegister(event)">
                <div class="form-group">
                    <label for="register-name">Full Name</label>
                    <input type="text" id="register-name" required placeholder="John Doe">
                </div>

                <div class="form-group">
                    <label for="register-email">Email Address</label>
                    <input type="email" id="register-email" required placeholder="your@email.com">
                </div>

                <div class="form-group">
                    <label for="register-password">Password</label>
                    <input type="password" id="register-password" required placeholder="Create a strong password" minlength="8">
                    <div class="password-requirements">Minimum 8 characters required</div>
                </div>

                <button type="submit" class="btn">Create Account</button>
            </form>

            <div class="back-link">
                <a href="/">← Back to Home</a>
            </div>
        </div>
    </div>

    <script>
        function switchTab(tabName) {
            // Hide all tabs
            document.querySelectorAll('.tab-content').forEach(tab => {
                tab.classList.remove('active');
            });

            document.querySelectorAll('.tab').forEach(tab => {
                tab.classList.remove('active');
            });

            // Show selected tab
            document.getElementById(tabName + '-tab').classList.add('active');
            event.target.classList.add('active');

            // Clear messages
            clearMessages();
        }

        function clearMessages() {
            document.querySelectorAll('.error-message, .success-message').forEach(msg => {
                msg.style.display = 'none';
                msg.textContent = '';
            });
        }

        function showError(tabName, message) {
            const errorEl = document.getElementById(tabName + '-error');
            errorEl.textContent = message;
            errorEl.style.display = 'block';
        }

        function showSuccess(tabName, message) {
            const successEl = document.getElementById(tabName + '-success');
            successEl.textContent = message;
            successEl.style.display = 'block';
        }

        async function handleLogin(event) {
            event.preventDefault();
            clearMessages();

            const email = document.getElementById('login-email').value;
            const password = document.getElementById('login-password').value;

            try {
                const response = await fetch('/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ email, password })
                });

                const data = await response.json();

                if (response.ok) {
                    // JWT is now in HttpOnly cookie (set by server)
                    showSuccess('login', 'Login successful! Redirecting...');
                    setTimeout(() => {
                        window.location.href = '/dashboard';
                    }, 1000);
                } else {
                    showError('login', data.error || data.message || 'Invalid email or password');
                }
            } catch (error) {
                showError('login', 'Network error. Please try again.');
                console.error('Login error:', error);
            }
        }

        async function handleRegister(event) {
            event.preventDefault();
            clearMessages();

            const name = document.getElementById('register-name').value;
            const email = document.getElementById('register-email').value;
            const password = document.getElementById('register-password').value;

            try {
                const response = await fetch('/auth/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ name, email, password })
                });

                const data = await response.json();

                if (response.ok) {
                    showSuccess('register', 'Account created successfully! Please login.');
                    setTimeout(() => {
                        switchTab('login');
                        document.getElementById('login-email').value = email;
                    }, 1500);
                } else {
                    showError('register', data.message || 'Registration failed. Please try again.');
                }
            } catch (error) {
                showError('register', 'Network error. Please try again.');
                console.error('Register error:', error);
            }
        }

        // Check if already logged in by trying to access a protected endpoint
        async function checkAuth() {
            try {
                const response = await fetch('/api/users/me', {
                    credentials: 'include'  // Include cookies in request
                });
                if (response.ok) {
                    window.location.href = '/dashboard';
                }
            } catch (error) {
                // Not logged in, stay on auth page
            }
        }
        checkAuth();
    </script>

    <footer style="text-align: center; padding: 20px; color: #666; font-size: 14px;">
        <a href="/privacy" style="color: #667eea; text-decoration: none; margin: 0 10px;">Privacy Policy</a>
        <span>•</span>
        <a href="/terms" style="color: #667eea; text-decoration: none; margin: 0 10px;">Terms of Service</a>
    </footer>
</body>
</html>
