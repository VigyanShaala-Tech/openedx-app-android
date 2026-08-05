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

The `CourseUnitContainerViewModel` is responsible for managing the display of course units.

- Added a private `markTopicCompleted(blockId: String)` method that launches a coroutine to call the interactor.
- Integrated this call into `getCurrentBlock()`. This ensures that every time a block becomes "active" (either by initial load or navigation), the completion API is triggered.

```kotlin
fun getCurrentBlock(): Block {
    val block = _descendantsBlocks.value.getOrNull(currentIndex) ?: blocks[currentVerticalIndex]
    _currentBlock.value = block
    _hierarchyPath.value = buildHierarchyPath(block)
    markTopicCompleted(block.id) // Trigger completion
    return block
}
```

## Architectural Refactoring

Alongside the API implementation, several UI components were refactored to better align with the project's architecture:

- **VsSignUpView.kt**: 
    - Removed hardcoded colors, replacing them with `MaterialTheme.appColors`.
    - Removed hardcoded typography overrides, using `MaterialTheme.appTypography` styles.
    - Moved hardcoded strings to `strings.xml`.
- **SignInView.kt**: 
    - Centralized hardcoded strings to `strings.xml`.
- **Global Resources**:
    - Added `core_mobile` and other shared strings to `core/strings.xml` and `auth/strings.xml`.
