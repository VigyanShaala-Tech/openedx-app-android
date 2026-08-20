# UI Architecture Refactoring Approach

This document describes the approach used to centralize UI constants and follow a more structured design system in the STEM Champions app.

## Goals
-   Remove hardcoded values (colors, dimensions, shapes) from UI components.
-   Ensure consistency across different screens.
-   Make it easier to update the design globally by changing values in a single place.

## Centralized Theme Components

### 1. `AppDimens`
A new `AppDimens` data class has been introduced to store common dimensions like:
-   `screenHorizontalPadding`: Standard padding for screen edges.
-   `defaultPadding`: Generic padding (8.dp).
-   `doublePadding`: Generic padding (16.dp).
-   `halfPadding`: Generic padding (4.dp).
-   `chipCornerRadius`: Standard radius for tags and chips.

It is accessed via `MaterialTheme.appDimens` in Composable functions.

### 2. `AppColors` Extensions
Added extension properties to `AppColors` for common color variations:
-   `primaryAlpha10`: `primary.copy(alpha = 0.1f)`
-   `primaryAlpha20`: `primary.copy(alpha = 0.2f)`

### 3. `AppShapes` Additions
Added `chipShape` to the `AppShapes` data class to standardize the appearance of interactive elements like filter chips or category tags.

## Screen Rotation Management
A hybrid approach is used for screen rotation:
1.  **Manifest**: `AppActivity` is set to `fullSensor` to allow the system to handle sensors.
2.  **Programmatic Lock**: In `AppActivity`, the orientation is programmatically locked to `portrait` by default.
3.  **Meeting Exception**: When `MeetingNotifier` broadcasts that a meeting is active, the orientation lock is released (`fullSensor`).
4.  **Fullscreen Logic**: `AppActivity` handles hiding/showing system bars based on orientation changes during a meeting.

## Usage in UI Components

UI components should now reference these values instead of using `dp` or `Color` directly.

**Before:**
```kotlin
Box(
    modifier = Modifier
        .clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.appColors.primary.copy(alpha = 0.1f))
        .padding(horizontal = 8.dp, vertical = 4.dp)
)
```

**After:**
```kotlin
Box(
    modifier = Modifier
        .clip(MaterialTheme.appShapes.chipShape)
        .background(MaterialTheme.appColors.primaryAlpha10)
        .padding(
            horizontal = MaterialTheme.appDimens.defaultPadding, 
            vertical = MaterialTheme.appDimens.halfPadding
        )
)
```

## Benefits
1.  **Readability**: Code is more semantic (e.g., `defaultPadding` vs `8.dp`).
2.  **Maintainability**: Changing the look and feel of the app (like padding or corner roundness) can be done in one file.
3.  **Consistency**: Ensures that all screens use the same set of design tokens.
