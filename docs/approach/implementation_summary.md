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

### Registration Form Dropdown Improvements
**Issue:** Users were getting stuck in searchable dropdowns (like University/College name) when their search returned no results, as there was no easy way to clear the search or select an "Other" option if the desired item was missing.

**Solution:**
- Added a "Clear" (X) icon to the search field in the `SelectionDialog` to allow users to quickly reset their search.
- Implemented a "No results found" state in the dropdown list when a search query doesn't match any options.
- If the registration field is configured to `allowOther`, an "Other" button is now displayed within the "No results found" state. Clicking this button automatically selects the "other" value, allowing users to proceed by manually entering their information in the "Please specify" field.

### New Enrollment Journey Implementation
**Issue:** The enrollment journey was updated with new API endpoints and requirements, including a new course catalog endpoint with `cohort_form_id`, explicit form retrieval, prefilling, eligibility checks, and file upload support.

**Solution:**
- **Course API Updates**: Updated `CourseApi.kt` with new endpoints:
    - `GET /api/v1/cohort-registration/{form_id}/form/` for form retrieval.
    - `POST /api/v1/cohort-registration/{form_id}/prefill/` for prefilling data.
    - `POST /api/v1/cohort-registration/{form_id}/check-eligibility/` for eligibility validation.
    - `POST /api/v1/cohort-registration/{form_id}/prepare-auth/` for submission.
    - `POST /api/v1/cohort-registration/{form_id}/upload/` (Multipart) for file uploads.
- **Repository and Interactor**: Implemented corresponding methods in `CourseRepository.kt` and `CourseRegistrationInteractor.kt`. The `uploadFile` method handles the creation of `MultipartBody.Part` and `RequestBody` for file transmission.
- **ViewModel Logic**:
    - Updated `CourseRegistrationViewModel.kt` to trigger file uploads automatically when a "file" type field is updated.
    - Added `isUploading` state to track and display file upload progress.
    - Refined the "Other" option logic to correctly manage `_other` suffix in submitted answers.
    - Ensured `cohort_form_id` from the updated catalog API is used to fetch the correct registration form.
- **Data Models**: Updated `CourseDetails` to include `cohort_form_id`.

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

## 23. Security Enhancements

### Enhanced Password Complexity Validation
- Improved password validation for both Account Creation (Sign Up) and Reset Password screens.
- Replaced the simple length-only check with a comprehensive complexity requirement.
- Passwords must now be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one numeric digit, and one special character.
- Centralized validation logic in `Validators.isValidPassword` utility for consistency across the application.
- Added descriptive error messages to guide users in creating secure passwords.

## 24. Social Registration Enhancements

### Custom Registration Request for Social Auth
- Modified the registration API request for social login flows to meet specific server requirements.
- Social registration now skips local password validation and generates a tailored JSON request body.
- Added new fields to `VsRegisterRequest`: `social_auth_provider` (e.g., "Continue with Google") and `total_registration_time` (seconds since screen was opened).
- Implemented automatic `username` generation from the email prefix during social registration.
- Switched `VsRegisterRequest` from form-encoded to JSON body format to support advanced fields and nested structures.

## 25. Performance and UI Optimizations

### Accelerated Course Home Loading
**Issue:** The course home page was taking 6-10 seconds to load due to multiple sequential API calls and excessive ViewPager pre-loading.

**Solution:**
- **Incremental Loading**: Refactored `CourseHomeViewModel.kt` to fetch essential data (Course Structure and Status) first for an immediate UI render. All other data (Announcements, Live Sessions, Progress, etc.) is now fetched in parallel and populates the UI cards incrementally as it arrives.
- **Pager Optimization**: Reduced `beyondViewportPageCount` to 1 in course and content pagers.
- **Lazy List Optimization**: 
    - Converted `CourseHomeScreen.kt` from a standard `Column` to a `LazyColumn` with unique item keys. This ensures only visible dashboard cards are composed, significantly improving scrolling performance.
    - Updated `LogistrationFragment.kt` (Discover Courses) to use stable item keys (`it.id`) in its course list, reducing redundant recompositions and layout passes.
    - Moved expensive list creations (like onboarding carousel items) inside `remember` blocks to prevent re-allocation during every scroll frame.
- **App-wide Navigation Optimization**: Set `offscreenPageLimit = 1` for main app ViewPagers to prevent unnecessary section initialization.
- **Improved Empty/Error States**: Replaced the generic "No data" screen with a descriptive "Loading course data..." state and a functional "Retry" button in case of failure.

## 26. New User Experience

### Automatic Redirection to Discovery
- Implemented automatic redirection to the "Discover" (Explore Courses) tab for users with no active or completed course enrollments.
- Added logic in `NewDashboardViewModel.kt` to check the enrollment status upon loading the dashboard.
- If a user has no courses in progress and no completed courses, the app now automatically triggers a navigation event to the Discovery section.
- This ensures that new users or users with empty dashboards are immediately presented with course options to explore, improving the onboarding experience.

## 27. Topic Completion and UI Architectural Refactoring

### Topic Completion API
- Implemented `POST /api/v1/courses/{course_id}/xblock/{block_id}/mark_completed` in `CourseApi.kt` and `DiscussionApi.kt`.
- Updated `CourseRepository.kt` and `CourseInteractor.kt` to support marking units as completed.
- **Comprehensive Triggering**: Integrated automatic completion triggering across all unit-related ViewModels (`HtmlUnitViewModel`, `PdfUnitViewModel`, `VideoUnitViewModel`, `VideoViewModel`, `DiscussionThreadsViewModel`).
- **Centralized Logic**: Refactored `CourseUnitContainerViewModel.kt` to ensure consistent triggering during any unit navigation or selection event.
- **Full-screen Support**: Explicitly added triggers to full-screen video fragments to mark completion on open.

## 28. Registration Form Enhancements and Fixes

### File Upload API Update
- Updated the `uploadFile` endpoint in `CourseApi.kt` to include `course_id` and `email` as part of the multipart request.
- Propagated these parameters through the repository and interactor layers.

### Frontend File Validation
- Implemented a `validateFile` method in `CourseRegistrationViewModel.kt`.
- **Size Limit**: Maximum file size allowed is 5MB.
- **Type Restrictions**: Only PDF, JPG, JPEG, PNG, DOC, and DOCX files are accepted.
- Users receive immediate feedback via SnackBar if a file fails validation.

### "Other" Option Manual Input Fix
- Resolved an issue where manual input for the "Other" option was overwriting the primary field selection.
- Introduced a separate storage key (`field_name_other`) for manual input text.
- Updated `CourseRegistrationScreen.kt` to bind the manual input field to this new key.
- Adjusted validation logic in `CourseRegistrationViewModel.kt` to properly require and validate this manual input when the "Other" option is selected.

### UI Refactoring for Consistency
- Refactored `VsSignUpView.kt` and `SignInView.kt` to strictly follow the project's design system.
- Replaced all hardcoded colors, typography, and dimensions with theme-aware properties from `MaterialTheme.appColors` and `MaterialTheme.appTypography`.
- Centralized remaining hardcoded strings in the Auth module to `strings.xml`.
- Improved maintainability and Dark Mode support for the authentication screens.

### Course Content Title Visibility
- Increased `maxLines` from 1 to 2 for course unit titles in the top toolbar and the sub-section header in `CourseUI.kt`.
- This ensures that long titles (common in STEM courses) are fully legible and not cut off.

### Course Banner Display Fix
- Updated `CollapsingLayout.kt` to use `ContentScale.Crop` and `Alignment.TopCenter` for the course banner image.
- This configuration ensures that the banner fully fills its designated height (240dp) without any white gaps, while specifically keeping the top portion of the image visible (cropping from the bottom).

## 29. Selection Dialog and University Search Improvements

### Enhanced Search Filtering
- Updated `SheetContent` in `ComposeCommon.kt` to use `contains` instead of `startsWith` for filtering options.
- This allows for more flexible matching (e.g., searching for "garwar" will now correctly find "Garware").

### Manual "Other" Option Fallback
- Implemented an automatic "Other" option within the selection dialog when a user's search query doesn't match any existing items.
- If the search query is not blank and no exact match is found in the list, an "${Other}: [query]" item is displayed at the bottom of the list.
- Clicking this item allows the user to use their search query as a manual entry, solving the issue of being stuck when a specific university or college is not in the predefined list.
- This implementation follows the project architecture by using central theme colors and string resources.

## 30. WebView "Google Chrome" Compatibility Optimization

### Global WebView "Full Access" Optimization
- Centralized WebView configuration into a new extension helper: `applyFullAccessSettings(url)` in `ViewExt.kt`.
- **Comprehensive Support across App**: Applied these optimized settings to `HtmlUnitFragment` (Course Units), `CatalogWebView` (Discovery), and `WebContentScreen` (General Web Content).
- **Mobile Chrome Identity**: Standardized `userAgentString` to a modern `MOBILE_CHROME_USER_AGENT` for **all** URLs. This ensures websites recognize the app as a full Google Chrome browser and provide their complete mobile experience.
- **Visual Stability**: Set `textZoom = 100` to prevent Android's system font size scaling from breaking responsive web layouts.
- **Interactive Parity**:
    - Enabled `builtInZoomControls = true` with `displayZoomControls = false` to support pinch-to-zoom without the dated UI buttons.
    - Set `cacheMode = WebSettings.LOAD_DEFAULT` for optimal performance and cookie persistence.
- **Advanced Capabilities**:
    - **Hardware Acceleration**: Enabled `android:hardwareAccelerated="true"` in the `AndroidManifest.xml` to ensure complex UIs render smoothly.
    - **Multiple Windows (Popups)**: Enabled `setSupportMultipleWindows(true)` by default to support auth popups and secondary windows.
    - **Third-Party Cookies**: Enabled support for cross-domain cookies across all versions from Lollipop onwards.
- **Meeting Specific Logic**: Specifically disabled `setSupportMultipleWindows` for Zoom/Meeting links to ensure the "More" menu and "Leave" confirmation modals function correctly within the primary view.


## 31. Registration and Social Auth Stability Fixes

### Corrected Registration Data
- **Social Provider Logic**: Fixed a bug in `VsSignUpViewModel.kt` where the `social_auth_provider` was being sent as `null` during social registration. It now correctly passes the provider name (e.g., "Continue with Google"), ensuring the server accepts the social signup.
- **Validation Completeness**: Updated `VsSignUpViewModel.kt` to include `access_token`, `provider`, and `client_id` in the registration validation step for social signups. This informs the server that it's a social registration, bypassing the mandatory password requirement and preventing "password too short" errors.
- **Robust API Error Handling**: Updated `AuthRepository.kt` to use the `.handleResponse()` helper in the `registerVs` method. This ensures that any non-success responses from the registration API (like "User already exists" or "Invalid data") are correctly caught and thrown as exceptions rather than being ignored.
- **User Feedback**: With proper exception propagation, the UI now correctly shows meaningful error messages in a SnackBar when registration fails, rather than leaving the user stuck on the signup screen.

## 32. Social Auth Redirection and Username Fixes

### Persistence of Social Identity
- **Access Token Forwarding**: Updated `AuthRouter.kt` and `AppRouter.kt` to include the `accessToken` in the `navigateToSignUp` navigation event.
- **SignIn Redirection**: Modified `SignInViewModel.kt` to capture the Google/Social token upon a failed token exchange (indicating a new user) and forward it to the signup screen.
- **Signup Initialization**: Refactored `VsSignUpFragment.kt` and `VsSignUpViewModel.kt` to accept and utilize this `initialToken`, ensuring the registration API call has the necessary social credentials.

### Robust Username Generation
- **Valid Social Usernames**: Improved the username generation logic in `VsSignUpViewModel.kt` for social signups. It now extracts the alphanumeric prefix from the email, preventing the full email address (e.g., `user@gmail.com`) from being sent as a username, which previously caused server validation failures.
## 33. Dependency Injection Fixes

### VsSignUpViewModel Configuration
- Updated `ScreenModule.kt` to correctly pass the `initialToken` parameter to `VsSignUpViewModel`.
- Fixed a compilation error where the `VsSignUpViewModel` Koin definition was missing the new `token` parameter added during the social auth refactoring.
- Updated the `viewModel` lambda to accept `token` as a parameter and pass it to the constructor.

## 34. Zoom Exit Confirmation Prompt

### Accidental Exit Prevention
- Implemented a confirmation prompt when the user attempts to leave a Zoom live session by pressing the back button (both in-app and hardware/gesture back).
- **Meeting Detection**: Added logic in `CourseUnitContainerFragment` to detect if the current active unit is a Zoom meeting or a meeting-related URL.
- **Custom Dialog**: Created `MeetingExitFragmentDialog` which displays the message: "Are you sure you want to leave the meeting?" with "Yes" and "No" options.
- **Navigation Flow**:
    - Selecting **Yes**: Confirms the exit and navigates the user back to the Course Outline screen.
    - Selecting **No**: Dismisses the dialog and keeps the user in the live session without interruption.
- **Centralized Handling**: Integrated this logic into `handleBackNavigation()` which is called by both the Android back press callback and the custom toolbar back button.

## 35. Orientation and Meeting State Management

### Meeting-Specific Rotation and Full Screen
**Requirement:** The application should only allow screen rotation and enter full-screen mode (hiding system bars) when a user is actively participating in a Zoom meeting or viewing a full-screen video. Other screens should remain locked in portrait mode.

**Solution:**
- **Centralized Management**: Utilized `MeetingNotifier` to broadcast the meeting status across the app.
- **Activity Integration**: Updated `AppActivity.kt` to observe `MeetingNotifier`.
    - When `isMeetingActive` is `true`, `requestedOrientation` is set to `ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR`.
    - `onConfigurationChanged` specifically hides system bars in landscape mode and shows them in portrait mode only when a meeting is active.
    - When `isMeetingActive` is `false`, `requestedOrientation` is reverted to `ActivityInfo.SCREEN_ORIENTATION_PORTRAIT`, and system bars are restored.
- **Robust Fragment Lifecycle Handling**:
    - Refined `HtmlUnitFragment.kt` to manage meeting state within `onResume` and `onPause` lifecycle methods.
    - This ensures that rotation is correctly enabled ONLY when the meeting unit is actually visible to the user, and immediately disabled when they swipe to a different unit or navigate away, preventing rotation leaks to other parts of the application.
- **Video Full Screen**: `VideoFullScreenFragment` continues to manage its own orientation lifecycle independently, ensuring it also supports rotation while active and resets to portrait upon exit.

## 36. UI Layout Fixes

### Dashboard Padding Adjustment
**Issue:** Extra white space was appearing above the bottom navigation bar on the Dashboard screen.

**Solution:**
- Removed `navigationBarsPadding()` from the `Scaffold` in `NewDashboardFragment.kt`.
- Since the dashboard is hosted within `MainFragment` which already positions its content above a custom `BottomNavigationView`, adding system navigation bar padding was causing redundant space.

## 37. Refined Orientation Reset

### Reliable Reversion to Manifest Defaults
**Issue:** Reports of orientation settings "leaking" (app remains rotatable) after exiting a meeting or full-screen video.

**Solution:**
- Updated `AppActivity.kt` to use `ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED` instead of `SCREEN_ORIENTATION_PORTRAIT` when resetting the meeting state.
- `SCREEN_ORIENTATION_UNSPECIFIED` tells the system to revert to the activity's default orientation defined in the `AndroidManifest.xml` (which is `portrait`). This is a more robust way to clear programmatic overrides and ensure the activity returns to its intended state across different Android versions and device types.

