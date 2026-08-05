# Topic Completion API Implementation

This document describes the implementation of the Topic Completion API, which allows the mobile application to mark course units as completed on the server as soon as they are opened by the user.

## Overview

Previously, course completion was handled by standard Open edX batch completion APIs or specific xblock handlers. The new requirement introduces a specific endpoint to mark an individual xblock/unit as completed.

## Changes

### 1. API Definition

Added the new endpoint to `CourseApi.kt`:

```kotlin
@POST("/api/v1/courses/{course_id}/xblock/{block_id}/mark_completed")
suspend fun markTopicCompleted(
    @Path("course_id") courseId: String,
    @Path("block_id") blockId: String
): ResponseBody
```

### 2. Repository and Interactor

- **CourseRepository**: Added `markTopicCompleted` to handle the network call. It includes basic error handling to prevent the app from crashing if the completion call fails.
- **CourseInteractor**: Exposed the new repository method to the presentation layer.

### 3. ViewModel Integration

To ensure the completion is triggered whenever any xblock related screen is opened, the logic has been integrated at multiple levels:

#### Centralized Container Trigger
The `CourseUnitContainerViewModel` is the primary manager for course units. It now includes a centralized `updateCurrentBlock(block: Block)` method that updates the current block state and triggers the completion API. This covers:
- Initial unit load.
- Manual navigation (Next/Previous).
- Selecting a specific unit from the video or sub-section list.

#### Individual Unit Triggers
For robustness and to cover scenarios where units might be opened independently, the completion trigger has been added to individual unit ViewModels:
- **HtmlUnitViewModel**: Triggers in `init`.
- **PdfUnitViewModel**: Triggers in `init`.
- **VideoUnitViewModel**: Triggers in `init` (covers standard and encoded videos).
- **VideoViewModel**: Triggers in `markTopicCompleted` (covers full-screen video players).
- **DiscussionThreadsViewModel**: Updated `markBlockCompleted` to call the new API.

#### Full-screen Video Triggers
Triggers were specifically added to the `onCreate` methods of full-screen fragments to ensure completion is marked as soon as the player opens:
- **VideoFullScreenFragment**
- **YoutubeVideoFullScreenFragment**

## UI Improvements

Alongside the API implementation, several UI components were refactored to better align with the project's architecture:

- **VsSignUpView.kt**: 
    - Removed hardcoded colors, replacing them with `MaterialTheme.appColors`.
    - Removed hardcoded typography overrides, using `MaterialTheme.appTypography` styles.
    - Moved hardcoded strings to `strings.xml`.
- **SignInView.kt**: 
    - Centralized hardcoded strings to `strings.xml`.
- **Global Resources**:
    - Added `core_mobile` and other shared strings to `core/strings.xml` and `auth/strings.xml`.

## UI Improvements

### Course Content Title Visibility
Improved the visibility of long course unit titles by allowing them to wrap across two lines instead of being truncated after the first line.
- **CourseUnitToolbar**: Increased `maxLines` to 2 and added `textAlign = TextAlign.Center`.
- **SubSectionUnitsTitle**: Increased `maxLines` to 2.
