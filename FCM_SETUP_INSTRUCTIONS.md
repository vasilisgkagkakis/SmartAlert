# Firebase Cloud Messaging v1 API Setup Instructions

## Overview
Your SmartAlert app now has Firebase Cloud Messaging (FCM) v1 API integrated. When an admin accepts an alert, the app will automatically send notifications to all users within 10km of the alert location.

## What's Been Added

### 1. Dependencies
- Firebase Messaging
- Google Auth Library for OAuth2
- OkHttp for HTTP requests

### 2. Services Created
- `SmartAlertFirebaseMessagingService`: Handles incoming FCM messages
- `FCMTokenManager`: Manages FCM tokens and saves them to Firestore
- `FCMNotificationSender`: Sends FCM v1 API notifications to nearby users
- `NotificationPermissionHelper`: Handles notification permissions

### 3. Updated Files
- AndroidManifest.xml: Added FCM permissions and service declarations
- MainActivity.java: Added FCM initialization and permission requests
- AdminViewAlertsActivity.java: Added notification sending when alerts are accepted

## Required Setup Steps

### Step 1: Create Assets Directory
Create the directory: `app/src/main/assets/`

### Step 2: Get Service Account Key
1. Go to Firebase Console → Project Settings → Service Accounts
2. Click "Generate new private key"
3. Download the JSON file
4. Rename it to `service-account-key.json`
5. Place it in `app/src/main/assets/service-account-key.json`

### Step 3: Update Project ID
Open `FCMNotificationSender.java` and replace `"your-firebase-project-id"` with your actual Firebase project ID from google-services.json.

### Step 4: User Location Storage
Ensure your users' locations are stored in Firestore with this structure:
```
users/{userId} {
  fcmToken: "user-fcm-token",
  location: GeoPoint(latitude, longitude),
  // other user fields...
}
```

## How It Works

1. **Token Management**: When users open the app, their FCM tokens are automatically saved to Firestore
2. **Permission Handling**: App requests notification permissions on Android 13+
3. **Alert Processing**: When admin accepts an alert:
   - Coordinates are extracted from the alert location
   - All users within 10km radius are found
   - FCM v1 API notifications are sent to those users
4. **Notification Display**: Users receive notifications with alert details

## Notification Features

- **Proximity-based**: Only users within 10km receive notifications
- **Rich notifications**: Include alert type, location, distance, and description
- **Android-specific**: Custom icon, color, and notification channel
- **High priority**: Ensures notifications are delivered promptly

## Testing

1. Create a test alert as admin
2. Accept the alert
3. Users within 10km should receive notifications
4. Check device logs for FCM token registration and notification sending

## Security Notes

- Service account key provides server-level access to FCM
- Keep the service-account-key.json file secure and never commit it to version control
- Add `service-account-key.json` to your .gitignore file
