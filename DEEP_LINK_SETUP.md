# Deep Link Setup & Production Deployment Guide

This guide explains how to maintain and update the Deep Linking functionality for the VigyanShaala application when moving from development to production.

## 1. Current Configuration
The app is currently configured to handle the following URLs:
- `https://apps.uat.vigyanshaala.com/learner-dashboard/?act=open&scr=Dashboard`
- `https://uat.vigyanshaala.com/dashboard?act=open&scr=Dashboard`

## 2. Play Store Deployment Steps (CRITICAL)

### A. Update SHA-256 Fingerprint
When you upload your app to Google Play, Google re-signs it with a production key. The current `assetlinks.json` only contains your local debug fingerprint.

1.  Log in to the [Google Play Console](https://play.google.com/console/).
2.  Select your app.
3.  Go to **Setup > App Integrity**.
4.  Copy the **SHA-256 certificate fingerprint** from the "App signing key certificate" section.
5.  Update your `assetlinks.json` file on the server (see section 3).

### B. Add Production Domains
If you move from `uat` to a production domain (e.g., `vigyanshaala.com`):
1.  **Update Manifest:** Add the new host to `app/src/main/AndroidManifest.xml`:
    ```xml
    <data android:host="vigyanshaala.com" />
    ```
2.  **Update Code:** Update `handleDeepLink` in `AppActivity.kt` to recognize the new host.
3.  **Host JSON:** Upload the `assetlinks.json` to the new production domain at `https://vigyanshaala.com/.well-known/assetlinks.json`.

---

## 3. Server-Side Configuration (assetlinks.json)
The file must be hosted at: `https://<your-domain>/.well-known/assetlinks.json`

### Content Template:
```json
[
  {
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
      "namespace": "android_app",
      "package_name": "org.app.vigyanshaala",
      "sha256_cert_fingerprints": [
        "02:06:EE:47:38:71:7D:CC:1D:03:D4:A4:9B:CD:06:5A:95:75:30:45:62:7A:47:92:06:13:EA:F4:C5:EA:B6:ED",
        "PASTE_YOUR_GOOGLE_PLAY_CONSOLE_SHA256_HERE"
      ]
    }
  }
]
```
*Note: You can keep multiple fingerprints in the list so that both Debug and Play Store versions work.*

---

## 4. Troubleshooting & Testing

### Verification Status
To check if Android has verified your domains:
```bash
adb shell pm get-app-links org.app.vigyanshaala
```
Look for `verified` status.

### Manual Verification (For Debugging)
If verification is stuck or you are testing a debug build without full server setup:
```bash
adb shell pm set-app-links --package org.app.vigyanshaala 2 all
```

### Test Deep Link Opening
```bash
adb shell am start -a android.intent.action.VIEW -d "https://apps.uat.vigyanshaala.com/learner-dashboard/?act=open\&scr=Dashboard" org.app.vigyanshaala
```

## 5. Play Console Verification
After uploading your app, check the **Grow > Deep Links** section in the Play Console. Google will provide a dashboard showing which domains are successfully verified and which ones need attention.
