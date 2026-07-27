# Achievements and Badges Implementation

This document describes the implementation of the Achievements and Badges feature in the VigyanShaala app.

## Overview
The Achievements screen provides learners with a view of their earned badges and badges currently in progress. This feature was initially implemented with static data and has been refactored to be fully dynamic.

## Dynamic Data Integration
- **API Source**: The achievements are fetched using `DashboardInteractor.getAllAchievements()`, which calls the `/api/v1/achievements/all/` endpoint.
- **Model Mapping**: The `AchievementsAllDto` contains:
    - `stats`: Key metrics like "Awards", "Trophies", "Completed".
    - `earned_badges`: Badges the user has already achieved.
    - `badges_in_progress`: Badges the user is currently working towards.
- **Removal of Hardcoded Fallbacks**: Previously, the app used hardcoded `BadgeProgressDto` objects when the API returned an empty list. These have been removed to ensure the app reflects the real-time state from the server.

## UI Components
- **Themed Styling**: UI components use `MaterialTheme.appColors` and `MaterialTheme.appTypography` for consistency and dark mode support.
- **Remote Icons**: `BadgeProgressItem` supports remote icons via `AsyncImage`. If `icon_url` is provided by the API, it is displayed.
- **Local Fallbacks**: For badges without remote icons, the UI provides local vector icon fallbacks based on the badge title (e.g., "Hour", "Research", "Community").

## Architectural Alignment
- **Modular Approach**: The implementation is encapsulated within the `dashboard` module.
- **State Management**: Uses `AchievementsViewModel` with `StateFlow` and `SharedFlow` for UI state and one-off messages.
- **Dependency Injection**: Koin is used for injecting the ViewModel and its dependencies.
