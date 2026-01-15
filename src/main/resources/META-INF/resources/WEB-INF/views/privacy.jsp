<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Privacy Policy - FinanceTracker</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            background: #f5f7fa;
            color: #333;
            line-height: 1.6;
        }

        .navbar {
            background: white;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            padding: 15px 0;
        }

        .navbar-content {
            max-width: 900px;
            margin: 0 auto;
            padding: 0 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .logo {
            font-size: 24px;
            font-weight: 700;
            color: #667eea;
            text-decoration: none;
        }

        .container {
            max-width: 900px;
            margin: 40px auto;
            padding: 0 20px;
            background: white;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            padding: 40px;
        }

        h1 {
            font-size: 36px;
            margin-bottom: 10px;
            color: #667eea;
        }

        .last-updated {
            color: #666;
            font-size: 14px;
            margin-bottom: 30px;
        }

        h2 {
            font-size: 24px;
            margin-top: 30px;
            margin-bottom: 15px;
            color: #333;
        }

        p {
            margin-bottom: 15px;
        }

        ul {
            margin-bottom: 15px;
            margin-left: 20px;
        }

        li {
            margin-bottom: 8px;
        }

        .back-link {
            display: inline-block;
            margin-top: 30px;
            color: #667eea;
            text-decoration: none;
            font-weight: 500;
        }

        .back-link:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <nav class="navbar">
        <div class="navbar-content">
            <a href="/" class="logo">💰 FinanceTracker</a>
        </div>
    </nav>

    <div class="container">
        <h1>Privacy Policy</h1>
        <p class="last-updated">Last Updated: January 15, 2026</p>

        <h2>1. Introduction</h2>
        <p>
            Welcome to FinanceTracker. We respect your privacy and are committed to protecting your personal data.
            This privacy policy explains how we collect, use, and safeguard your information when you use our service.
        </p>

        <h2>2. Information We Collect</h2>
        <p>We collect the following types of information:</p>
        <ul>
            <li><strong>Account Information:</strong> Name, email address, and encrypted password</li>
            <li><strong>Financial Data:</strong> Bank account information, balances, and transactions accessed through Plaid</li>
            <li><strong>Technical Data:</strong> IP address, browser type, and usage patterns</li>
        </ul>

        <h2>3. How We Use Your Information</h2>
        <p>We use your information to:</p>
        <ul>
            <li>Provide and maintain our finance tracking service</li>
            <li>Sync your bank accounts and transactions via Plaid</li>
            <li>Improve and personalize your experience</li>
            <li>Communicate with you about your account</li>
            <li>Ensure security and prevent fraud</li>
        </ul>

        <h2>4. Data Security</h2>
        <p>We implement industry-standard security measures to protect your data:</p>
        <ul>
            <li><strong>Encryption:</strong> All sensitive data including Plaid access tokens are encrypted at rest using AES-256-GCM</li>
            <li><strong>Secure Authentication:</strong> Passwords are hashed using bcrypt, and sessions use HttpOnly secure cookies</li>
            <li><strong>HTTPS:</strong> All data transmission is encrypted using TLS/SSL</li>
            <li><strong>CSRF Protection:</strong> We implement Cross-Site Request Forgery protection</li>
            <li><strong>Access Controls:</strong> You can only access your own data</li>
        </ul>

        <h2>5. Third-Party Services</h2>
        <p>
            We use <strong>Plaid</strong> to connect to your bank accounts. Plaid's privacy policy can be found at
            <a href="https://plaid.com/legal/#consumers" target="_blank">https://plaid.com/legal/#consumers</a>.
            We do not have access to your banking credentials - they are securely handled by Plaid.
        </p>

        <h2>6. Your Rights</h2>
        <p>You have the right to:</p>
        <ul>
            <li><strong>Access:</strong> View all data we have about you</li>
            <li><strong>Correction:</strong> Update inaccurate information</li>
            <li><strong>Deletion:</strong> Delete your account and all associated data at any time</li>
            <li><strong>Data Portability:</strong> Export your data</li>
            <li><strong>Withdraw Consent:</strong> Disconnect bank accounts or delete your account</li>
        </ul>

        <h2>7. Data Retention</h2>
        <p>
            We retain your data for as long as your account is active. When you delete your account,
            all your data including user profile, bank accounts, transactions, and Plaid connections
            are permanently deleted from our systems.
        </p>

        <h2>8. Children's Privacy</h2>
        <p>
            Our service is not intended for users under the age of 18. We do not knowingly collect
            personal information from children under 18.
        </p>

        <h2>9. Changes to This Policy</h2>
        <p>
            We may update this privacy policy from time to time. We will notify you of any changes
            by posting the new privacy policy on this page and updating the "Last Updated" date.
        </p>

        <h2>10. Contact Us</h2>
        <p>
            If you have any questions about this privacy policy or our data practices, please contact us
            at privacy@financetracker.com.
        </p>

        <a href="/" class="back-link">← Back to Home</a>
    </div>
</body>
</html>
