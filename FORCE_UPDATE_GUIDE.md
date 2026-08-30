# Force Update Implementation Guide

The application already has a mechanism to handle force updates and recommended updates. This is managed by the `AppUpgradeInterceptor` which monitors every API response for specific headers.

## 1. How it Works
The `AppUpgradeInterceptor` checks for two custom HTTP headers in any API response from your backend. Depending on the values provided, the app will either block the user with a "Required Update" screen or show a "Recommended Update" banner.

## 2. Backend Requirements
To trigger an update, your backend should include the following headers in its responses:

| Header | Description | Example |
| :--- | :--- | :--- |
| `EDX-APP-LATEST-VERSION` | The version name of the latest available app. | `1.0.9` |
| `EDX-APP-VERSION-LAST-SUPPORTED-DATE` | The ISO 8601 date until which the current version is supported. | `2024-08-27T00:00:00Z` |

### Triggering a "Force Update"
If the `EDX-APP-VERSION-LAST-SUPPORTED-DATE` provided in the header is **less than (earlier than)** the current date/time on the device, the app will trigger an **Upgrade Required** event. 
- This will show a full-screen block that prevents the user from using the app until they update from the Play Store.

### Triggering a "Recommended Update"
If the `EDX-APP-VERSION-LAST-SUPPORTED-DATE` is **greater than (later than)** the current date/time, but the `EDX-APP-LATEST-VERSION` is different from the app's current version, a **Recommended Update** banner will be shown.
- The user can dismiss this and continue using the app.

### Using HTTP 426 Status Code
Alternatively, if your backend returns an **HTTP 426 (Upgrade Required)** status code for any request, the app will immediately show the "Upgrade Required" screen.

---

## 3. Play Store Integration
The "Update" button in the app will automatically redirect the user to the Play Store page for your application package (`org.openedx.app` or your configured `APPLICATION_ID`).
