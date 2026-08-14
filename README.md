# CampusGig — Android Client

CampusGig is a modern, student-focused freelancing and campus collaboration platform built for Android using **Jetpack Compose**, **Kotlin Coroutines / Flow**, **Retrofit**, and **Socket.IO**.

---

## Features

- **Authentication & Security**: JWT-based login/registration with encrypted credential storage via Android Jetpack `EncryptedSharedPreferences`.
- **Gigs Marketplace**: Browse, filter, search, post, edit, and apply for campus gigs with category filtering and budget tracking.
- **Application Management**: Submit proposals, review received applications, accept/reject applicants, and withdraw applications.
- **Real-Time Chat**: Live 1-on-1 messaging powered by Socket.IO with typing indicators and online presence tracking.
- **Communities & Networking**: Discover and join campus tech/interest communities with category-based exploration.
- **User Profiles & Reviews**: Public profile view, rating and review breakdown, and personal stats dashboard.
- **In-App Notifications**: Real-time updates on application status changes, new gigs, and message alerts.

---

## Tech Stack

- **UI Framework**: Jetpack Compose (Material 3)
- **Language**: Kotlin 2.0+
- **Architecture**: MVVM (Model-View-ViewModel) + StateFlow
- **Networking**: Retrofit 2 + OkHttp 4 (with interceptors & logging)
- **Real-time Communication**: Socket.IO Client Java
- **Security**: AndroidX Security Crypto (`EncryptedSharedPreferences`)
- **JSON Serialization**: Gson
- **Async Programming**: Coroutines & Kotlin Flow

---

## Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- Android SDK 36 (Minimum SDK: 24)
- JDK 17+

### Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/not-a-hack-er/campus_gig-frontend.git
   cd campus_gig-frontend
   ```

2. **Configure environment & endpoints:**
   Copy `keystore.properties.example` to `keystore.properties`:
   ```bash
   cp keystore.properties.example keystore.properties
   ```
   Update `BASE_URL` and `SOCKET_URL` to match your local backend IP or hosted server.

3. **Build and Run:**
   - Open the project in Android Studio.
   - Sync Gradle project.
   - Run on an Android Emulator or physical device.

---

## Build Commands

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Assemble Debug APK
./gradlew assembleDebug

# Assemble Release APK (requires keystore configuration)
./gradlew assembleRelease
```
