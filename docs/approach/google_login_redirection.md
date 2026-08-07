# Google Login Redirection & UI Refactoring

## Overview
This document describes the implementation of the Google Login redirection flow and the architectural improvements made to the authentication screens.

## 1. Google Login Redirection Flow
Previously, if a user tried to log in with Google and they were not registered in the system, the app would show an "Account Not Found" error message.

### Changes:
- **`SignInViewModel.kt`**: Modified the `exchangeToken` function. When an `EdxError.InvalidGrantException` occurs (indicating the Google account is not linked to any user), the app now automatically navigates to the `VsSignUpFragment`.
- **Pre-filled Data**: The user's `name` and `email` obtained from the Google response are passed to the Sign-Up screen, ensuring they are auto-populated for a better user experience.

## 2. Architectural & UI Improvements
To maintain consistency with the project's architecture, we refactored several UI components to remove hardcoded values.

### Changes:
- **`SignInView.kt`**:
    - Replaced hardcoded color values (e.g., `Color(0xFF455A64)`, `Color.Red`) with theme-aware attributes from `MaterialTheme.appColors`.
    - Improved the usage of `stringResource` for all user-facing text.
- **`VsSignUpView.kt`**:
    - Cleaned up string concatenations to follow best practices.
    - Ensured that all UI elements adhere to the `OpenEdXTheme`.

## 3. Benefits
- **Improved Onboarding**: Reduces friction for new users by guiding them directly to registration.
- **Theme Support**: Ensures consistent appearance in both light and dark modes.
- **Maintainability**: Centralizes UI styling in the theme, making future changes easier to manage.
