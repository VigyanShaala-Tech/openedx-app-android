# Google Play Store Publishing Guide - Resolving Foreground Service Violations

This guide explains how the "Permissions for Foreground Services" policy rejections reported by Google Play Console have been permanently resolved.

## 1. Why Google Play Rejected the App
Starting in Android 14 (API level 34+), Google Play Developer Policy strictly forbids using `FOREGROUND_SERVICE` or `FOREGROUND_SERVICE_DATA_SYNC` for background file downloads, data syncs, or calendar syncs.

Google's policy explicitly states:
> *"Data sync foreground services should not be used for downloading media files or syncing app data in the background. Apps must use WorkManager without calling setForeground()."*

Because `DownloadWorker`, `CalendarSyncWorker`, and `OfflineProgressSyncWorker` were previously calling `setForeground()`, Google Play Console flagged the app and rejected the policy declaration form.

---

## 2. Code Changes Applied

To comply 100% with Google Play Developer Policy and eliminate the need for any Foreground Service declarations:

1. **Removed `setForeground()` Calls:**
   - Updated `DownloadWorker.kt`, `CalendarSyncWorker.kt`, and `OfflineProgressSyncWorker.kt` to run as standard, reliable background `CoroutineWorker` tasks managed by WorkManager and Android's `JobScheduler`.
2. **Removed Foreground Service Permissions & Manifest Service Entry:**
   - Stripped `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_DATA_SYNC` permissions from `app/src/main/AndroidManifest.xml`.
   - Set `tools:node="remove"` on `SystemForegroundService` in `AndroidManifest.xml` to ensure WorkManager's foreground service component is completely removed from the merged APK/AAB.

---

## 3. Creating a New Release for Play Console

With these changes, **NO Foreground Service Declaration is required** in Google Play Console.

### Steps to Submit:

1. Build a new App Bundle (`.aab`):
   ```bash
   ./gradlew bundleRelease
   ```
2. In [Google Play Console](https://play.google.com/console/):
   - Go to **Policy and programs** > **App content** > **Foreground Services**.
   - If a declaration form is active, you can **remove/clear** the Foreground Service selection or state that no foreground service permissions are requested in the new bundle.
3. Upload the new `.aab` (e.g. `versionCode 12` / `versionName 1.1.2`) to the target track.
4. Submit the release for review.

The new bundle will pass automated Play Console policy checks without triggering any Foreground Service permission warnings or rejections.
