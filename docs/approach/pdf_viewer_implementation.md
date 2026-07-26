# PDF Viewer Implementation

This document describes the implementation of the PDF viewer for course units.

## Overview

The PDF viewer allows students to view PDF content directly within the course unit container, similar to how HTML and Video content is displayed.

## Implementation Details

### 1. Dependencies

Added the following dependencies to the `course` module:
- `io.github.ahmerafzal1:ahmer-pdfium:1.9.2`
- `io.github.ahmerafzal1:ahmer-pdfviewer:2.0.1`

### 2. Domain Model Updates

Updated the `Block` domain model and its data/room counterparts to include:
- `isPdfBlock`: A helper property to identify PDF blocks.
- `pdfWebUrl`: The URL to the PDF file.
- Default values for new fields to maintain backward compatibility with existing code and tests.

### 3. PDF Unit Fragment and ViewModel

Created `PdfUnitFragment` and `PdfUnitViewModel` in the `org.openedx.course.presentation.unit.pdf` package.

- **PdfUnitViewModel**: Handles the downloading of the PDF file to the application's cache directory using `OkHttpClient`. It manages the UI state (Loading, Loaded, Error).
- **PdfUnitFragment**: A Compose-based fragment that:
    - Triggers the PDF download on initialization.
    - Displays a loading indicator while the file is being downloaded.
    - Uses `AndroidView` to wrap the `PDFView` from the library for displaying the downloaded PDF.
    - Handles error states with the project's standard `FullScreenErrorView`.

### 4. Adapter Integration

Updated `CourseUnitContainerAdapter` to recognize PDF blocks and navigate to the `PdfUnitFragment`.

```kotlin
    block.isPdfBlock -> {
        createPdfUnitFragment(block)
    }
```

### 5. Dependency Injection

Registered `PdfUnitViewModel` in `ScreenModule.kt` to allow it to be injected into the fragment.

## Architectural Considerations

- **Resource Management**: Followed the project's architecture by using `MaterialTheme.appColors` and `BaseViewModel`.
- **Robustness**: Moved network operations to the ViewModel using `viewModelScope` and `Dispatchers.IO`.
- **Consistency**: Integrated the new feature seamlessly into the existing course unit container logic.
