# Google Play Store Publishing Guide - Resolving Foreground Service Violations

This guide provides step-by-step instructions to resolve the "Permissions for Foreground Services" policy violations reported by Google Play Console.

## 1. Code Changes Applied
The following changes have been made to the codebase to comply with Android 14+ (API 34/35/36) requirements:

- **Manifest Updates**: Added `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_DATA_SYNC` permissions in `app/src/main/AndroidManifest.xml`.
- **WorkManager Compliance**: The `SystemForegroundService` in the manifest now correctly declares `android:foregroundServiceType="dataSync"`, which matches the usage in `DownloadWorker` and `CalendarSyncWorker`.
- **Permission Requests**: The app already requests `POST_NOTIFICATIONS`, which is required for foreground service notifications on Android 13+.

## 2. Play Console Action Required (CRITICAL)

The Play Store violation mentions **Media Projection**, but this app primarily uses Foreground Services for **Data Sync** (Downloads and Calendar Synchronization). You must update your declaration in the Play Console.

### Steps to update the declaration:

1. Log in to the [Google Play Console](https://play.google.com/console/).
2. Select your app: **STEM Champions by VigyanShaala**.
3. Navigate to **Policy and programs** > **App content**.
4. Find the **Foreground Services** section and click **Manage** (or **Start** if it's a new declaration).
5. **REMOVE / DESELECT** the **Media Projection** category.
6. **SELECT** the **Data Sync** category.
7. Provide the following justifications if prompted:
   - **User-facing feature**: "Offline Downloads and Calendar Synchronization".
   - **Description**: "The app uses foreground services to ensure that course content downloads and academic calendar synchronization continue reliably even when the user switches to another app. This provides a seamless offline learning experience and keeps students notified of upcoming course dates."
8. **Submit** the declaration.

## 3. Creating a New Release

After updating the declaration and applying the code changes:

1. Build a new App Bundle (`.aab`):
   ```bash
   ./gradlew bundleRelease
   ```
2. Upload the new `.aab` to the **Production** track (or the track with the violation).
3. Ensure the `versionCode` is higher than the current one (e.g., increment to `3`).
4. Submit the release for review.

## 4. Architecture & UI Improvements
As part of this update, we have also refactored the following to align with the project's architecture:
- Removed hardcoded colors in `CollapsingLayout.kt`, `SignInView.kt`, and `VsSignUpView.kt`.
- Standardized UI components to use `MaterialTheme.appColors`.
- Enhanced Google Sign-In flow to automatically redirect unregistered users to the Sign-Up screen with pre-filled details.
- This ensures the app correctly supports both **Light** and **Dark** modes across all screens and provides a smoother onboarding experience.
