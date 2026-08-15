# Picture-in-Picture (PiP) Support for Zoom Meetings

This document explains the implementation of Picture-in-Picture (PiP) support for Zoom meetings in the STEM Champions app.

## Overview

To improve student engagement, the app supports PiP mode when a user is attending a live Zoom meeting. This allows students to continue listening to and watching the meeting while using other apps (like Mentimeter for interactive polls).

## Implementation Details

### 1. Identifying Zoom Meetings
Zoom meetings are identified by checking the URL against common patterns defined in `AppDataConstants.ZOOM_URL_PATTERNS`. This centralizes the identification logic and makes it easy to update if Zoom changes their URL structure.

### 2. Meeting State Tracking
A `MeetingNotifier` (located in `org.openedx.core.system.notifier`) is used to broadcast whether a meeting is currently active. 
- `CourseUnitContainerFragment` updates this state in its `onResume` (sets to `true` if it's a meeting) and `onPause` (sets to `false`).

### 3. Entering PiP Mode
PiP mode is triggered in three ways:
- **Home Button (API 31+)**: The app uses `setAutoEnterEnabled(true)` to automatically transition to PiP when the user swipes up to Home during a meeting.
- **Home Button (API 26-30)**: `AppActivity.onUserLeaveHint()` triggers `enterPictureInPictureMode()` manually.
- **Back Button**: When the user presses the Back button during a meeting, a **Confirmation Dialog** is shown. The user can choose to:
    - **Minimize**: Enters PiP mode (Android 8.0+).
    - **Leave Meeting**: Exits the meeting and closes the fragment.
    - **Cancel**: Stays in the meeting.

### 4. Confirmation Dialog
The `MeetingExitFragmentDialog` has been updated to include a "Minimize" button when PiP is supported by the device. This ensures the user has a choice between continuing the session in the background or leaving it entirely.

### 4. Manifest Configuration
`AppActivity` is configured in `AndroidManifest.xml` to support PiP, rotation, and seamless resizing:
```xml
<activity
    android:name=".AppActivity"
    ...
    android:supportsPictureInPicture="true"
    android:resizeableActivity="true"
    android:screenOrientation="fullSensor"
    android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation|keyboardHidden|keyboard|navigation|uiMode|density">
```

### 5. Browser-like Behavior
To ensure Zoom meetings behave correctly (similar to Chrome):
- **User Agent**: Zoom URLs use a **Mac Desktop User Agent** by default to force "Join from browser". A modern Pixel 8 Mobile UA is used for all other sites.
- **Popup Handling**: SSO and other popups are handled within the same WebView to maintain session state.
- **Fullscreen Support**: `WebChromeClient.onShowCustomView` is implemented to allow the meeting to fill the screen when the "Fullscreen" button is clicked.
- **File Downloads**: `DownloadListener` is added to handle file downloads from the meeting (like chat attachments).
- **Security Settings**: Safe Browsing is disabled specifically for Zoom to avoid subdomains being blocked by Android's security filter.
- **Protocols**: Custom schemes like `zoommtg://` are supported via Intents.

## How to Test

1. Navigate to a course unit that contains a Zoom meeting.
2. Join the meeting.
3. Press the **Home** button. The app should minimize into a small floating window.
4. Tapping the floating window should bring the app back to full screen.
5. While in the meeting, press the **Back** button. The app should also enter PiP mode instead of exiting the meeting immediately.
6. To exit the meeting, close the PiP window or navigate back once the meeting is in full screen (if not in PiP).

## Constraints
- PiP is only supported on Android 8.0 (API level 26) and above.
- For devices below API 26, the app falls back to the previous behavior (showing a "Leave Meeting?" confirmation dialog).
