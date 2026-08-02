# Implementation Summary

This document outlines the recent changes made to the project to address build issues, runtime crashes, and production environment optimizations.

## 1. Resolved Build Incompatibilities

### Kotlin 2.2.10 and Bugsee Plugin
**Issue:** A `NoSuchMethodError` occurred during the IR (Intermediate Representation) transformation phase of the Kotlin compiler. This was caused by the Bugsee Gradle plugin's Compose transformer being incompatible with the IR API changes in Kotlin 2.2.10.

**Solution:**
- Disabled Bugsee's Compose instrumentation in `app/build.gradle`.
- Upgraded Bugsee SDK to `7.0.4`.
- This bypasses the problematic compiler plugin while maintaining core crash reporting features.

## 2. Production Build Optimizations

### Excluding Debugging Tools
**Requirement:** Bugsee and Shake SDKs should not be included in the production release build.

**Solution:**
- Updated `app/build.gradle` to use `developImplementation` and `stageImplementation` for Bugsee and Shake dependencies.
- This ensures these libraries are only included in development and staging APKs.
- Modified `OpenEdXApp.kt` to use reflection (`Class.forName`) when initializing these SDKs. This allows the app to compile and run in production even when the classes are missing from the classpath.

### Facebook SDK Auto-Initialization
**Issue:** The Facebook SDK was auto-initializing and logging errors when no App ID was provided in the manifest.

**Solution:**
- Disabled Facebook auto-initialization, app events, and advertiser ID collection via `AndroidManifest.xml` meta-data.
- Added safe manual initialization in `OpenEdXApp.kt` that only runs if a valid configuration is provided and the SDK is present on the classpath.

## 3. Stability Improvements

### Networking Error Handling
**Issue:** The app crashed when receiving a non-JSON error response (e.g., an HTTP 500 HTML page) because `HandleErrorInterceptor` failed to parse the body as JSON and threw a raw `IOException` with a large message.

**Solution:**
- Refactored `HandleErrorInterceptor.kt` to be more robust.
- Added catch blocks for parsing failures to throw a generic `EdxError.UnknownException` instead of crashing.
- Removed the inclusion of large response bodies in exception messages to avoid potential IPC/logging issues and keep logs clean.
- Ensured all errors are wrapped in `EdxError` subclasses for consistent handling across the app.

## 4. Architectural Considerations

### Static Resources and Common Constants
- Consolidated feature flags and SDK configurations into `config.yaml` files.
- Used flavor-specific resource directories for theming and environment-specific strings.
- Implementation follows a modular approach where feature modules (`auth`, `discovery`, etc.) encapsulate their own logic and dependencies, while the `app` module orchestrates global state and SDK lifecycle.

## 5. New Features

### PDF Viewing Support
- Integrated `ahmer-pdfviewer` to support native PDF viewing in course units.
- Updated `Block` models to handle PDF metadata.
- Implemented `PdfUnitFragment` and `PdfUnitViewModel` using Jetpack Compose and Koin.

## 6. WebView Enhancements

### Improved XBlock Support (Google Calendar, SGA, Surveys)
**Issues:**
- Google Calendar not loading in course units.
- Staff Graded Assignment (SGA) "Upload" button not opening file manager.
- Survey blocks having interaction issues (selecting options).

**Solution:**
- Updated `HtmlUnitFragment.kt` and `CourseUnitContainerAdapter.kt` to support `EDX_SGA` block type.
- Implemented `WebChromeClient.onShowFileChooser` in `HtmlUnitFragment` using `ActivityResultLauncher` with `FLAG_GRANT_READ_URI_PERMISSION` to handle file uploads securely.
- Enhanced WebView settings in `HtmlUnitFragment` for maximum compatibility:
    - Enabled `databaseEnabled`, `domStorageEnabled`, and `javaScriptEnabled`.
    - Disabled `setSupportMultipleWindows` to prevent `IllegalArgumentException` crashes when content attempts to open popup windows (common in some XBlocks).
    - Added `CookieManager.getInstance().setAcceptThirdPartyCookies(true)` for embedded content.
    - Implemented Desktop User Agent override for `google-calendar` URLs.
    - Set `mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW`.
    - Enabled `isFocusable` and `isFocusableInTouchMode` for better interaction.

## 7. Dynamic Achievements and Badges

### Refactoring to API-driven Content
**Issue:** The "My Achievements" screen was using hardcoded fallback data for "Badges in Progress".

**Solution:**
- Removed hardcoded fallback lists from `AchievementsViewModel.kt`.
- Updated `AchievementsView.kt` to support remote `icon_url` for badges using `AsyncImage`.
- Aligned UI styling with the project's theme system by replacing hardcoded colors with `MaterialTheme.appColors`.

## 8. UI Refinement and Static Resource Refactoring

### Course Dashboard Cleanup
**Issue:** Redundant "VigyanShaala" text in course header and course banner image cut off at the top.

**Solution:**
- Removed the redundant organization name from `HeaderContent.kt`.
- Adjusted banner image alignment to `Alignment.TopCenter` in `CollapsingLayout.kt` to ensure the top part of the graphics is always visible.
- Refactored hardcoded colors in `CourseContainerFragment.kt`, `HeaderContent.kt`, and `CollapsingLayout.kt` to use the central theme system.

### Static Resource Refactoring
- Added `warningRed` and `cardDivider` colors to `AppColors` and `Theme.kt`.
- Replaced various hardcoded hex colors and standard Compose colors (like `Color.White`) with theme-aware colors to ensure consistency and better Dark Mode support.
- Centralized hardcoded strings from `HeaderContent.kt` and `LiveSessionsCardContent.kt` to `strings.xml`.

## 9. Session and Content Display Improvements

### OauthRefreshTokenAuthenticator Refinement
- Modified `OauthRefreshTokenAuthenticator.kt` to prevent immediate forced logout on token refresh failure.
- Removed `appNotifier.send(LogoutEvent(true))` calls from `handleTokenExpired` and `handleInvalidToken` to allow for potential retry or more graceful failure handling.
- Added logging to track token refresh failures without disrupting the user session prematurely.

### CourseHomeViewModel Enhanced Debugging
- Added comprehensive logging to `CourseHomeViewModel.kt`'s `getCourseDataInternal` and `combine` logic to help identify why "No content" might be displayed despite successful API responses.
- Improved error handling in `combine` flow to capture and log specific failure points during course data initialization.

## 10. Course Notification API and UI Updates

### API Endpoint Update
- Updated the course notifications API endpoint in `CourseApi.kt` from `/api/v1/get/course/notifications/{course_id}/` to `/api/v1/get/notifications/course/{course_id}/`.
- Removed the hardcoded UAT domain from the endpoint to use the configured API host.

### Notification UI Logic
- Refined the notification dot logic in `CourseContainerViewModel.kt`. The `haveNewNotification` state is now determined by checking if there are any unread notifications in the list (`unreadCount > 0`), rather than relying solely on the `haveNewNotification` boolean from the API.
- Updated `HeaderContent.kt` to conditionally display the red dot on the notification icon only when `haveNewNotification` is true.

## 11. Global Theme and Filter Improvements

### Forcing Light Mode
- Application is now forced to Light Mode regardless of system settings.
- Implemented `AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)` in `OpenEdXApp.kt`.
- Updated `OpenEdXTheme` in `Theme.kt` to always use `LightColorPalette`.
- Configured `AppActivity.kt` to always use dark status bar icons (light mode appearance).

### Discovery Filter Regression Fix
- Restored the active filter chips in `LogistrationFilters.kt`.
- When a filter is selected, a chip with a "Close" icon appears below the filter dropdowns, allowing users to easily remove individual filters.
- Re-implemented `ActiveFilterChip` component with appropriate styling and click handling.

## 12. Discovery Search and Pagination Refinements

### Search Query Persistence
- Fixed an issue in `LogistrationFragment.kt` where the search query would be wiped out upon submitting the search.
- Set `clearOnSubmit = false` in the `SearchBar` component to ensure the query remains visible after the user presses search.

### Correct Course Count Display
- Updated `DiscoveryUIState.kt` to include a `totalCount` field in the `Courses` state.
- Modified `LogistrationViewModel.kt` and `NativeDiscoveryViewModel.kt` to extract the total count from the API response (pagination metadata) and pass it to the UI.
- Updated the display text in `LogistrationFragment.kt` to show "Showing X of Y courses", where Y is the actual total count from the server (e.g., 14 instead of just the page size).

## 13. Registration Form Multi-select Fix

### Multi-select Delimiter Update
- Fixed a bug in the registration form where multi-select fields incorrectly split values containing commas (e.g., "Data Science, AI and ML").
- Switched the internal delimiter used for joining and splitting selected values from a comma (`,`) to a pipe (`|`).
- Updated `CourseRegistrationScreen.kt` and `CourseRegistrationViewModel.kt` to ensure consistent use of the new delimiter in both the UI and the state management logic.
- Ensured that submitted values are still correctly converted to a `List` before being sent to the API.

## 17. Registration Form Enhancements

### Registration Form "Other" Option Visibility
**Issue:** When selecting "Other" or "Others" in a dropdown (select/multi-select) field in the registration form, the "Please specify" text input field was not appearing if the internal value of the option didn't exactly match "other" or "others" (even if the display label did).

**Solution:**
- Enhanced the `isOtherSelected` check in both `CourseRegistrationScreen.kt` and `CourseRegistrationViewModel.kt`.
- The check now looks at both the internal `value` and the display `label` of the selected options.
- If either the value or the label (case-insensitive) matches "other" or "others", or if the internal value is `"__other__"`, the "Please specify" text field is displayed and treated as a required field if the parent field is required.

### Registration Form Field Support and Validation
**Issue:** Some registration form fields were not being rendered because their types (`number`, `file`) were not implemented. Additionally, some prefilled fields were not properly activating the "Next" button.

**Solution:**
- Implemented `number` and `file` field types in `CourseRegistrationScreen.kt` and `CourseRegistrationUI.kt`.
- Added support for `KeyboardType.Number` and `KeyboardType.Email` in `VsRegistrationTextField`.
- Improved prefill data processing to ensure that all prefilled values (including multi-select lists) are correctly stored in the ViewModel state.
- Enhanced form validation logic to ensure that missing but required fields (previously unrendered) are now visible and correctly validated, allowing the "Next" button to enable when all visible required fields are filled.

### Registration Form File Upload and Prefill Logic
**Issue:** The "file" field type lacked a functional file picker. Also, if the server sent prefilled data that didn't match the available dropdown options, the app wouldn't show that information or automatically select "Other".

**Solution:**
- Implemented a file picker for "file" type fields using `ActivityResultContracts.GetContent()`.
- Added logic in `CourseRegistrationViewModel.kt` to handle prefilled values not present in the options list.
- If a prefilled value is missing from options, the app now automatically selects the "Other" option (if available) and places the actual prefilled value into the "Please specify" text field.
- Fixed a bug where manual "Other" values were overwriting the dropdown's "Other" selection due to sharing the same key; they now correctly use a `_other` suffix.

### Registration Form Step Transition Fix
- Fixed an issue where the scroll position was maintained when transitioning between steps in the registration form.
- Added a `LaunchedEffect` to reset the scroll position to the top whenever the `currentStep` changes, ensuring a consistent starting point for each page.

## 18. Course Dashboard Navigation Fixes

### Announcement and About this Course Redirection
**Issue:** In the course dashboard "More" section, both "Announcement" and "About this Course" were incorrectly redirecting to the same "Announcements" screen.

**Solution:**
- Added `navigateToCourseDetail(fm, courseId)` method to `CourseRouter.kt`.
- Implemented `navigateToCourseDetail` in `AppRouter.kt` to show the native `CourseDetailsFragment`.
- Updated `CourseContainerFragment.kt` to call `navigateToCourseDetail` when "About this Course" is clicked, ensuring it now correctly shows the course overview, curriculum, instructors, and reviews.

## 19. Search Query Persistence in Explore Courses

### Persisting Search Query in ViewModels
**Issue:** When searching for courses in "Explore Courses" (Logistration or Discovery), navigating to course details, and then pressing back, the search results remained but the query text in the search bar was cleared.

**Solution:**
- Updated `LogistrationViewModel.kt` and `CourseSearchViewModel.kt` to store the search query in a `MutableLiveData` property.
- Updated `LogistrationFragment.kt` and `CourseSearchFragment.kt` to observe this `searchQuery` from the ViewModel.
- Implemented a `LaunchedEffect` in both screens to synchronize the `TextFieldValue` of the search bar with the persisted query from the ViewModel.
- This ensures that the search bar always reflects the current search state, even after fragment re-creation when navigating back from course details.

## 20. Search Refresh on Query Clear

### Automatic List Refresh
**Issue:** Clearing the search query (either by clicking the 'X' button or manually deleting all text) did not refresh the course list to show the default/full results.

**Solution:**
- Updated `LogistrationFragment.kt` and `CourseSearchFragment.kt` to trigger a search update whenever the search query is cleared.
- Modified `LogistrationViewModel.kt` to revert to the default discovery list when the search term is empty.
- Modified `CourseSearchViewModel.kt` to fetch the full course list when the search query is cleared, instead of showing an empty screen.

## 21. Achievement Screen UI Fixes

### Stat Card Text Alignment
- Fixed the alignment of labels ("Badges", "Certificates", etc.) in the `StatCard` component on the Achievements screen.
- Added `textAlign = TextAlign.Center` to ensure that text is correctly centered underneath the numeric values even when the labels wrap to multiple lines.

## 22. Course Progress UI Fixes

### Quiz Score Chart Labels
- Fixed an issue where the "Assignment" label in the Quiz Score chart was wrapping into two lines.
- Increased the label width to `80.dp` and set `maxLines = 1` to ensure labels stay on a single line and align properly with the chart bars.
