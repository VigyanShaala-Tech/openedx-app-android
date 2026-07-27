# PDF Viewer Implementation

This document describes the implementation of the PDF viewer for course units.

## Overview

The PDF viewer allows students to view PDF content directly within the course unit container, integrated seamlessly with the existing course navigation.

## Implementation Details

### 1. Dependencies

The implementation uses the `ahmer-pdfviewer` library for rendering PDF files:
- `io.github.ahmerafzal1:ahmer-pdfium:1.9.2`
- `io.github.ahmerafzal1:ahmer-pdfviewer:2.0.1`

### 2. Domain Model Updates

The `Block` model (and its Data/Room counterparts) has been updated to include:
- `isPdfBlock`: A boolean property to identify PDF content blocks.
- `pdfWebUrl`: A string property containing the source URL of the PDF file.
- Default values are provided to ensure backward compatibility and prevent build errors in call sites like mocks and UI previews.

### 3. PDF Unit Fragment and ViewModel

#### PdfUnitViewModel
A lightweight ViewModel that handles business logic and interaction with the course system.
- `notifyCompletionSet()`: Signals that the student has viewed the PDF unit, which can be used to track course progress.

#### PdfUnitFragment
A Compose-based fragment that serves as the entry point for PDF units.
- **Download Logic**: Uses a `LaunchedEffect` to download the PDF from the provided URL into the application's cache directory. This ensures the download is tied to the lifecycle of the Composable and handles configuration changes correctly.
- **UI States**:
    - **Loading**: Displays a standard `CircularProgressIndicator` while the file is being downloaded.
    - **Error**: Displays an error message if the download fails.
    - **Content**: Uses `AndroidView` to wrap the `PDFView` from the library, loading the PDF from the temporary file.

### 4. Integration

The `CourseUnitContainerAdapter` has been updated to recognize the `PDF` block type and navigate to the `PdfUnitFragment`.

```kotlin
    block.isPdfBlock -> {
        createPdfUnitFragment(block)
    }
```

## Architectural Considerations

- **Resource Standardization**: Used `MaterialTheme.appColors` and `MaterialTheme.appTypography` for consistency with the rest of the application.
- **Lifecycle Management**: Leveraging `LaunchedEffect` for network operations ensures that resources are managed efficiently within the Compose framework.
- **Cache Management**: PDFs are stored in the application's cache directory, which the system can manage and clear if storage is low.
