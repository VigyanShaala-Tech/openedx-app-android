# Picture-in-Picture (PiP) Implementation Approach

This document describes the technical approach used to implement Picture-in-Picture (PiP) mode for Zoom meetings.

## Architecture

The implementation follows the project's modular architecture:

1.  **Centralized Constants (`core` module)**:
    -   `AppDataConstants.ZOOM_URL_PATTERNS` contains the list of URL fragments used to identify Zoom meetings. This replaces multiple hardcoded checks across the codebase.

2.  **Reactive State Management**:
    -   A `MeetingNotifier` in the `core` module provides a `Flow<Boolean>` representing the active meeting state.
    -   Koin dependency injection is used to share this notifier as a singleton across the app.

3.  **Fragment-to-Activity Communication**:
    -   `CourseUnitContainerFragment` (in the `course` module) and `WebContentFragment` (in the `core` module) detect if the current content is a meeting.
    -   They update the `MeetingNotifier` state accordingly.
    -   `AppActivity` (in the `app` module) collects the state from `MeetingNotifier`.

4.  **PiP Triggering**:
    -   **Auto-Enter (API 31+)**: `AppActivity` sets `setAutoEnterEnabled(active)` in the `PictureInPictureParams`. This provides the smoothest transition on modern devices.
    -   **Legacy Home Trigger (API 26-30)**: `AppActivity.onUserLeaveHint()` is overridden to call `enterPictureInPictureMode(params)` manually.
    -   **Manual/Back Trigger**: Both `CourseUnitContainerFragment` and `WebContentFragment` intercept the Back button. If a meeting is active, they show a **Confirmation Dialog** (`MeetingExitFragmentDialog`).
    -   **Confirmation Dialog**: Moved to the `core` module to be accessible globally. It offers options to "Minimize" (PiP), "Leave", or "Cancel".

5.  **Browser-Like WebView Configuration**:
    -   **Enhanced User Agent**: Uses a standard Desktop Chrome UA for Zoom URLs to ensure the "Join from browser" option is available.
    -   **Window Management**: Sets `setSupportMultipleWindows(true)` and handles `onCreateWindow` by returning the current WebView, keeping the session consistent.
    -   **Scheme Handling**: Specifically handles custom schemes (non-http/s like `zoommtg://`) via Intents in `shouldOverrideUrlLoading`.
    -   **Resource Access**: Enables DOM storage, Database access, and local file access to support advanced web client features.
    -   **Security**: Disables Safe Browsing for Zoom specifically to avoid false-positive blocks.
    -   **Hardware Acceleration**: Enabled in `AndroidManifest.xml` at the application level.

## Component Responsibilities

### Core Module
-   **`AppDataConstants`**: Source of truth for URL patterns and User Agents.
-   **`MeetingNotifier`**: Broadcasts meeting state changes.
-   **`ViewExt` & `WebContentScreen`**: Centralized WebView configuration.
-   **`WebContentFragment`**: Handles direct Zoom URL links and PiP transitions.

### Course Module
-   **`CourseUnitContainerFragment`**: Manages the lifecycle of course-based meeting units.
-   **`HtmlUnitFragment`**: Adjusts UI (padding, cache) for Zoom blocks.

### App Module
-   **`AppActivity`**: Final orchestrator for PiP transitions and manifest configuration.
