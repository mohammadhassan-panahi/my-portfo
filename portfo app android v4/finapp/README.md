<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/5e00673f-ec3b-479d-bded-043eaa62b644

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
7. If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.


## وضعیت این نسخه

این نسخه چند اصلاح مهم Production را دارد:
- حذف `fallbackToDestructiveMigration` و استفاده از migrationهای Room
- داده‌های نمونه فقط در buildهای Debug ساخته می‌شوند
- API key فقط در Cloudflare Worker secret نگهداری می‌شود
- Import پشتیبان اعتبارسنجی و transaction اتمیک دارد
- PIN دارای salt تصادفی و rate-limit/lockout است
- فروش دارایی ورودی‌ها و موجودی را اعتبارسنجی می‌کند
- حذف خریدی که قبلاً در فروش استفاده شده مسدود می‌شود

### راه‌اندازی قیمت زنده

1. در `brsapi-proxy` مقدار واقعی `BRSAPI_KEY` را فقط با Wrangler Secret ثبت کن.
2. `wrangler deploy` را اجرا کن.
3. URL Worker را در `.env` به عنوان `PROXY_BASE_URL (optional)` قرار بده.
4. کلید API را هرگز در `.env`، README، Git یا APK قرار نده.

### نکته Backup

فایل JSON پشتیبان عمداً قابل خواندن است؛ بنابراین آن را مانند اطلاعات مالی محرمانه نگهداری کن. در نسخه بعدی بهتر است Backup رمزنگاری‌شده با رمز عبور کاربر اضافه شود.
