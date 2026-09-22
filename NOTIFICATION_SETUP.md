# Push Notification Integration & Redirection Guide

This document provides the complete specification and sample JSON payloads for sending Push Notifications via Firebase Cloud Messaging (FCM) to the VigyanShaala Android application.

When a user taps a notification, the app parses the payload and performs **one-click redirection** directly to the intended course, unit, discussion, live class, or screen.

---

## 1. Notification Payload Architecture

FCM messages contain two main keys:
1. `notification`: Sets the title, body, and sound in the system notification tray.
2. `data`: Contains key-value key pairs parsed by the app for one-click deep linking.

```json
{
  "to": "<DEVICE_FCM_REGISTRATION_TOKEN>",
  "notification": {
    "title": "Notification Title",
    "body": "Notification body message description."
  },
  "data": {
    "screen_name": "<TARGET_SCREEN_TYPE>",
    "course_id": "<OPTIONAL_COURSE_ID>",
    "component_id": "<OPTIONAL_COMPONENT_ID>"
  }
}
```

---

## 2. Key Parameter Reference Guide

| Parameter Key | Alternative Alias Keys Supported | Description / Example |
| :--- | :--- | :--- |
| `screen_name` | `notification_type`, `type`, `screen`, `action` | Destination target screen (e.g. `course_dashboard`, `course_videos`). |
| `course_id` | `courseId`, `CId`, `course`, `c_id` | OpenEdX Course ID (e.g. `course-v1:VigyanShaala+STEM101+2026`). |
| `component_id` | `componentId`, `path_id` | Specific block or unit ID within a course. |
| `topic_id` | `topicId` | Specific discussion topic ID. |
| `thread_id` | `threadId` | Specific discussion thread or post ID. |
| `comment_id` | `commentId` | Specific comment or response ID in a discussion. |
| `mId` | `meeting_id`, `meetingId` | Zoom / Live class meeting ID. |
| `token` | — | Token for password reset redirection. |
| `activationId` | `activation_id` | Account activation ID. |

---

## 3. FCM Payload Samples for All Redirection Targets

### 1. Open Enrolled Course Dashboard
Redirects the user directly to the home screen of an enrolled course.
```json
{
  "to": "/topics/enrolled_learners",
  "notification": {
    "title": "Welcome to She for STEM Incubator!",
    "body": "Your course is now active. Tap here to start learning."
  },
  "data": {
    "screen_name": "course_dashboard",
    "course_id": "course-v1:VigyanShaala+STEM101+2026"
  }
}
```

---

### 2. Open Specific Course Unit / Component
Navigates the user directly to a specific unit (quiz, assignment, or reading block).
```json
{
  "to": "<DEVICE_FCM_TOKEN>",
  "notification": {
    "title": "New Quiz Published",
    "body": "Quiz 3: Pre-program Survey is ready. Click to take the quiz."
  },
  "data": {
    "screen_name": "course_component",
    "course_id": "course-v1:VigyanShaala+STEM101+2026",
    "component_id": "block-v1:VigyanShaala+STEM101+2026+type@sequential+block@quiz3"
  }
}
```

---

### 3. Open Course Videos Tab
Directs the user to the recorded videos section of a course.
```json
{
  "to": "<DEVICE_FCM_TOKEN>",
  "notification": {
    "title": "New Recorded Lecture Uploaded",
    "body": "Personal SWOT Analysis video is now available."
  },
  "data": {
    "screen_name": "course_videos",
    "course_id": "course-v1:VigyanShaala+STEM101+2026"
  }
}
```

---

### 4. Open Course Dates / Schedule Tab
Redirects the user to the course schedule/deadlines tab.
```json
{
  "to": "<DEVICE_FCM_TOKEN>",
  "notification": {
    "title": "Upcoming Deadline Warning",
    "body": "Assignment 2 is due in 24 hours. Check your schedule."
  },
  "data": {
    "screen_name": "course_dates",
    "course_id": "course-v1:VigyanShaala+STEM101+2026"
  }
}
```

---

### 5. Open Course Handouts
Navigates the user to handouts and supplementary resources for a course.
```json
{
  "to": "<DEVICE_FCM_TOKEN>",
  "notification": {
    "title": "New Study Material Uploaded",
    "body": "Handout PDF for Week 3 is now available for download."
  },
  "data": {
    "screen_name": "course_handout",
    "course_id": "course-v1:VigyanShaala+STEM101+2026"
  }
}
```

---

### 6. Open Course Announcements
Navigates the user to course announcements.
```json
{
  "to": "<DEVICE_FCM_TOKEN>",
  "notification": {
    "title": "Important Instructor Announcement",
    "body": "Dr. Arohi posted an update regarding tomorrow's schedule."
  },
  "data": {
    "screen_name": "course_announcement",
    "course_id": "course-v1:VigyanShaala+STEM101+2026"
  }
}
```

---

### 7. Open Discussion Forum / Specific Thread
Opens a specific post or discussion thread.
```json
{
  "to": "<DEVICE_FCM_TOKEN>",
  "notification": {
    "title": "Someone replied to your post",
    "body": "Mentor Dr. Arohi replied to your question in STEM Career Exploration."
  },
  "data": {
    "screen_name": "discussion_post",
    "course_id": "course-v1:VigyanShaala+STEM101+2026",
    "thread_id": "651a2b3c4d5e6f7g8h9i"
  }
}
```

---

### 8. Open Live Zoom Meeting / Class
Opens the live meeting screen directly.
```json
{
  "to": "<DEVICE_FCM_TOKEN>",
  "notification": {
    "title": "Live Class Starting Now!",
    "body": "Zoom session 'Discovering STEM' has started. Tap to join."
  },
  "data": {
    "screen_name": "meeting",
    "course_id": "course-v1:VigyanShaala+STEM101+2026",
    "mId": "81234567890"
  }
}
```

---

### 9. Open Discovery / Explore Course Details
Navigates an un-enrolled or browsing user to a specific course details page.
```json
{
  "to": "/topics/all_users",
  "notification": {
    "title": "New Program Announced!",
    "body": "Explore 'She for STEM - Telangana (2026)'. Tap to view details."
  },
  "data": {
    "screen_name": "discovery_course_detail",
    "course_id": "course-v1:VigyanShaala+STEM_TELANGANA+2026"
  }
}
```

---

### 10. Open User Profile
Navigates the user to their profile tab.
```json
{
  "to": "<DEVICE_FCM_TOKEN>",
  "notification": {
    "title": "Update Your Profile",
    "body": "Add your educational background to receive personalized mentorship."
  },
  "data": {
    "screen_name": "profile"
  }
}
```

---

### 11. Password Reset / Account Activation
Redirects the user to complete password reset or account activation.
```json
{
  "to": "<DEVICE_FCM_TOKEN>",
  "notification": {
    "title": "Reset Your Password",
    "body": "Tap to complete resetting your account password."
  },
  "data": {
    "screen_name": "PasswordReset",
    "token": "abc123xyz_reset_token"
  }
}
```

---

## 4. Full List of Supported `screen_name` Values

| `screen_name` Value | Destination Screen |
| :--- | :--- |
| `"dashboard"` | Main home dashboard |
| `"course_dashboard"` or `"CourseWare"` | Enrolled course home screen |
| `"course_videos"` | Course videos tab |
| `"course_dates"` | Course dates/schedule tab |
| `"course_discussion"` | Course discussion forum |
| `"course_handout"` | Course handouts tab |
| `"course_announcement"` | Course announcements |
| `"course_component"` | Specific unit/component page |
| `"discussion_topic"` | Specific discussion topic |
| `"discussion_post"` | Specific discussion post/thread |
| `"discussion_comment"` | Specific comment/reply |
| `"meeting"` | Zoom live class |
| `"discovery"` | Course discovery tab |
| `"discovery_course_detail"` | Course details page in discovery |
| `"program"` | Programs page |
| `"profile"` or `"user_profile"` | User profile page |
| `"PasswordReset"` | Password reset screen |
| `"accActivation"` | Account activation screen |

---

## 5. Testing Notifications via ADB

You can test one-click push notification deep linking locally using ADB commands:

### Test Course Dashboard One-Click Opening:
```bash
adb shell am start -a android.intent.action.VIEW \
  -e screen_name "course_dashboard" \
  -e course_id "course-v1:VigyanShaala+STEM101+2026" \
  org.app.vigyanshaala.stemchampions/org.openedx.app.AppActivity
```

### Test Zoom Live Meeting Opening:
```bash
adb shell am start -a android.intent.action.VIEW \
  -e screen_name "meeting" \
  -e course_id "course-v1:VigyanShaala+STEM101+2026" \
  -e mId "81234567890" \
  org.app.vigyanshaala.stemchampions/org.openedx.app.AppActivity
```
