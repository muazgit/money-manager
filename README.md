# Taka Manager (টাকা ম্যানেজার) ৳

A full-featured, offline-first personal finance and Hisab Khata Android application designed specifically with dual-language support (English & Bengali), multi-wallet management, debt tracking (দেনা-পাওনা), monthly budget management, financial analytics, and an integrated In-App Update system.

---

## 🌟 Key Features

1. **Dashboard & Financial Summary**:
   - Live net worth and balance calculation across all accounts.
   - Income & Expense cards with instant percentage breakdowns.
   - Quick Action buttons for fast transaction logging, wallet transfers, and budget setups.
   - Recent transaction lists with interactive filtering.

2. **Multi-Wallet Account Management**:
   - Supports Cash (নগদ টাকা), bKash (বিকাশ), Nagad (নগদ), Bank Account (ব্যাংক অ্যাকাউন্ট), and custom wallets.
   - Transfer money directly between accounts with auto-logging.
   - Add, edit, archive, or delete custom accounts.

3. **Transaction History & Filter Engine**:
   - Filter by Date Range (All Time, Today, This Week, This Month, This Year).
   - Filter by Type (All, Income, Expense).
   - Real-time search by description, category, or note.
   - Delete single transactions or swipe to manage.

4. **Dena-Paona (দেনা-পাওনা / Debts & Loans)**:
   - Track Receivables (টাকা পাবেন) and Payables (টাকা দিবেন).
   - Mark debts as settled / cleared with one tap.
   - Due date tracking with visual status tags.

5. **Monthly Category Budgets**:
   - Set monthly spending limits per category.
   - Progress bar indicators (Green: Under limit, Orange: Near limit, Red: Over budget).

6. **Interactive Analytics**:
   - Visual income vs. expense progress bars and breakdowns.
   - Category-wise spending percentage calculation.

7. **Bengali & English Localization**:
   - Toggle seamlessly between **বাংলা** and **English**.
   - Bengali numeral converter (`১২৩,৪৫৬ ৳` or `123,456 ৳`).

8. **Theme Persistence**:
   - Dark Mode and Light Mode persisted in `SharedPreferences`.

9. **In-App Updater**:
   - Auto-checks for new releases from your GitHub repository (`muazgit/money-manager`).
   - One-tap APK download and in-place upgrade.

---

## 🚀 How to Publish & Release Updates to Users

When you push new changes to GitHub and want users to receive the update:

1. **Build a new APK**:
   - In AI Studio, click the **Settings** menu and select **Generate APK**.
   - Or run `./gradlew assembleRelease` locally.

2. **Create a Release on GitHub**:
   - Go to `https://github.com/muazgit/money-manager/releases/new`
   - Tag the release (e.g., `v1.1.0`).
   - Upload the generated `.apk` file as an asset to this release.

3. **Update `version.json` in your repository root**:
   ```json
   {
     "versionCode": 2,
     "versionName": "1.1.0",
     "releaseNotes": "• Added new features\n• Performance optimizations",
     "downloadUrl": "https://github.com/muazgit/money-manager/releases/download/v1.1.0/taka-manager.apk",
     "mandatory": false
   }
   ```
4. **Result**:
   All active users will automatically see a popup prompt: **"New Update Available! (v1.1.0)"** and can tap **Update Now** to install.

---

## 🛠 Tech Stack & Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Local Database**: Room (SQLite) with Coroutines & StateFlow
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Networking**: OkHttp3 for update checks and APK downloading
