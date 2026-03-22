# Todo — Android App

> **Finally, todos that judge you.**

A full-stack Android productivity app built entirely in Java with Android Studio. Features Google Sign-In, real-time cloud sync, priority management, AdMob monetization, and a premium subscription flow.

---

## Screenshots

| Login | Home | Add Task |
|-------|------|----------|
| ![Login](screenshots/login.png) | ![Home](screenshots/home.png) | ![Add Task](screenshots/add_task.png) |

---

## Features

- **Google Sign-In** — One-tap authentication via Firebase Auth
- **Real-time Sync** — Tasks stored and synced instantly using Cloud Firestore
- **Priority Management** — High, Medium, and Low priority levels with color indicators
- **Category Tagging** — Organize tasks by Work, Personal, Health, or Other
- **Live Dashboard** — Total, Done, and Pending stats update in real time
- **Add Task Bottom Sheet** — Clean Material Design interface with priority and category chips
- **Swipe to Delete** — Swipe left or right with undo support via Snackbar
- **AdMob Integration** — Banner ads for free users
- **Go Premium** — Remove ads with a monthly subscription via Google Play Billing v6
- **Profile Photo** — Loaded from Google account using Glide
- **Offline-aware** — Firestore handles offline caching automatically

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java |
| IDE | Android Studio |
| Authentication | Firebase Auth (Google Sign-In) |
| Database | Cloud Firestore (real-time) |
| Ads | Google AdMob |
| Billing | Google Play Billing v6 |
| Image Loading | Glide |
| UI Components | Material Design 3 |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 34 (Android 14) |

---

## Project Structure

```
app/src/main/
├── java/com/utkarsh/todo/
│   ├── SplashActivity.java          # Branded splash screen
│   ├── LoginActivity.java           # Google Sign-In screen
│   ├── MainActivity.java            # Main dashboard
│   ├── AddTaskBottomSheet.java      # Bottom sheet for adding tasks
│   ├── TodoAdapter.java             # RecyclerView adapter
│   ├── TodoItem.java                # Task data model
│   ├── FirestoreManager.java        # All Firestore operations
│   ├── PremiumManager.java          # Play Billing subscription
│   ├── SwipeToDeleteCallback.java   # Swipe gesture handler
│   └── TodoApp.java                 # Application class (AdMob init)
├── res/
│   ├── layout/
│   │   ├── activity_splash.xml
│   │   ├── activity_login.xml
│   │   ├── activity_main.xml
│   │   ├── item_todo.xml
│   │   └── bottom_sheet_add_task.xml
│   ├── drawable/                    # Vector icons and backgrounds
│   └── values/                      # Colors, strings, themes
└── AndroidManifest.xml
```

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 8 or higher
- A Firebase project
- A Google AdMob account
- A Google Play Console account (for billing)

### Setup

**1. Clone the repository**

```bash
git clone https://github.com/yourusername/todo-android.git
cd todo-android
```

**2. Firebase Setup**

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a new project
3. Add an Android app with package name `com.utkarsh.todo`
4. Download `google-services.json` and place it in the `app/` folder
5. Enable **Google Sign-In** under Authentication → Sign-in method
6. Create a **Firestore Database** (start in test mode)

**3. Firestore Security Rules**

Paste these rules in Firestore → Rules:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null
                         && request.auth.uid == userId;
    }
  }
}
```

**4. AdMob Setup**

Replace the test IDs in the project with your real IDs:

- `AndroidManifest.xml` → `com.google.android.gms.ads.APPLICATION_ID`
- `MainActivity.java` → `adView.setAdUnitId(...)`

> Test IDs are already set for development. Do **not** use test IDs in production.

**5. Play Billing (Optional)**

1. Upload a signed APK/AAB to Google Play Console
2. Go to Monetize → Subscriptions → Create subscription
3. Set Product ID to `premium_monthly`
4. Set your price and activate

**6. Build and Run**

```bash
./gradlew assembleDebug
```

Or press **Run** in Android Studio.

---

## Dependencies

```groovy
// Firebase
implementation platform('com.google.firebase:firebase-bom:32.7.0')
implementation 'com.google.firebase:firebase-auth'
implementation 'com.google.firebase:firebase-firestore'

// Google Sign-In
implementation 'com.google.android.gms:play-services-auth:20.7.0'

// Material + AndroidX
implementation 'com.google.android.material:material:1.11.0'
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'androidx.recyclerview:recyclerview:1.3.2'

// AdMob
implementation 'com.google.android.gms:play-services-ads:22.6.0'

// Play Billing
implementation 'com.android.billingclient:billing:6.1.0'

// Image Loading
implementation 'com.github.bumptech.glide:glide:4.16.0'
implementation 'de.hdodenhof:circleimageview:3.1.0'
```

---

## Free vs Premium

| Feature | Free | Premium |
|---------|------|---------|
| Google Sign-In | ✅ | ✅ |
| Cloud sync (Firestore) | ✅ | ✅ |
| Priority + Categories | ✅ | ✅ |
| Swipe to delete | ✅ | ✅ |
| Banner ads | Shown | ✅ Removed |
| Upgrade banner | Shown | ✅ Removed |
| Crown badge | ❌ | ✅ |

---

## Permissions

| Permission | Reason |
|-----------|--------|
| `INTERNET` | Firebase, AdMob, billing |
| `POST_NOTIFICATIONS` | Download progress (Android 13+) |

---

## License

```
MIT License

Copyright (c) 2024 Utkarsh

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.
```

---

## Author

**Utkarsh**  
Android Developer  
[LinkedIn]([https://linkedin.com/in/yourprofile](https://www.linkedin.com/in/utkarshsahu9906/)) · [GitHub]([https://github.com/yourusername](https://github.com/UtkarshSahu9906/Todo-do-the-thing))

---

> Built with Java. Shipped with pride. Zero Kotlin. No excuses.
