# 🎓 CampusGig — Android Application

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" alt="CampusGig Logo" width="100" height="100" style="border-radius: 20%;" />
</p>

<p align="center">
  <b>A modern, high-performance campus freelancing and student networking platform built with Jetpack Compose.</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.0.21-purple?style=for-the-badge&logo=kotlin" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-Material_3-blue?style=for-the-badge&logo=jetpackcompose" alt="Compose" />
  <img src="https://img.shields.io/badge/Android_SDK-24_.._36-green?style=for-the-badge&logo=android" alt="Android SDK" />
  <img src="https://img.shields.io/badge/Socket.io-Realtime_Chat-black?style=for-the-badge&logo=socketdotio" alt="Socket.IO" />
  <img src="https://img.shields.io/badge/License-MIT-orange?style=for-the-badge" alt="License" />
</p>

---

## 📖 Overview

**CampusGig** bridges the gap between campus talent and student opportunities. It empowers college students to post freelance gigs, apply for projects with tailored proposals, collaborate in real time with WebSocket chat, build campus communities, and showcase verified skills through peer reviews and public portfolios.

The app is built following **Modern Android Architecture (MVVM)**, **Unidirectional Data Flow (UDF)**, and **Material Design 3**, delivering a fluid, responsive, and secure experience.

---

## ✨ Key Features

### 🔐 1. Authentication & Security
- **JWT-Based Authentication**: Secure login and registration with validation.
- **Encrypted Local Storage**: Session tokens and user credentials stored using AndroidX `EncryptedSharedPreferences`.
- **Automatic Token Injection**: Retrofit interceptor automatically attaches Bearer tokens and handles `401 Unauthorized` token expiration scenarios.

### 💼 2. Freelancing & Gig Marketplace
- **Explore Gigs**: Real-time listing with dynamic category filtering, keyword search, and budget range sorting.
- **Post & Manage Gigs**: Create gigs with rich descriptions, required skill tags, deadline dates, and fixed/hourly budgets.
- **Edit & Delete**: Full lifecycle management for gig owners.

### 📑 3. Proposals & Applications Management
- **Custom Proposals**: Applicants submit expected budgets and cover proposals.
- **Applicant Review Dashboard**: Gig creators can review applicant profiles, ratings, and proposals in one unified screen.
- **Status Workflow**: Accept or reject applicants; applicants can withdraw pending applications.

### 💬 4. Real-Time Chat & Messaging
- **Instant 1-on-1 Messaging**: Powered by **Socket.IO** client with auto-reconnection and room-based isolation.
- **Real-Time Indicators**: Live typing status, message timestamps, and unread badges.
- **Media Viewer**: Full-screen preview support for shared images.

### 👥 5. Campus Communities & Discussions
- **Topic-Based Hubs**: Create and join student communities (e.g., *Android Dev, AI, Open Source, Placement Prep*).
- **Interactive Posts**: Share knowledge, ask questions, and engage in threaded community discussions.

### 👤 6. Public Profiles & Peer Reviews
- **Portfolio Showcase**: GitHub links, portfolio URLs, bio, and college affiliations.
- **Ratings & Reviews**: 5-star rating system with verified client reviews.
- **Stats Dashboard**: Dynamic analytics tracking completed gigs, active applications, and total earnings.

### 🔔 7. Notification System
- Real-time in-app notification center for new gig proposals, application status updates, and message alerts.
- Filter, mark as read, and bulk "mark all as read" functionality.

---

## 🏗 Architecture & Tech Stack

The project adheres to Google's official Android Architecture Guidelines with clean separation of concerns:

```
campusgig_frontend/
├── app/
│   ├── src/main/java/com/abpvt/campusgig_frontend/
│   │   ├── core/                    # Core infrastructural utilities & networking
│   │   │   ├── network/             # Retrofit client, OkHttp interceptors, API service
│   │   │   ├── storage/             # EncryptedSharedPreferences session storage
│   │   │   └── utils/               # Constants, Resource wrappers, ViewModelFactory
│   │   │
│   │   ├── data/                    # Data Layer
│   │   │   ├── model/               # Domain data models & request/response DTOs
│   │   │   └── repository/          # Data repositories coordinating Network & Local Cache
│   │   │
│   │   ├── features/                # UI & Presentation Layer (Feature-First MVVM)
│   │   │   ├── applications/        # My Applications, Proposal details & Review
│   │   │   ├── auth/                # Login, Register & Password reset
│   │   │   ├── chat/                # Real-time chat, SocketManager & Media view
│   │   │   ├── communities/         # Communities, Posts & Discussion threads
│   │   │   ├── gigs/                # Gig marketplace, Create/Edit gig & Applicant list
│   │   │   ├── home/                # Feed, category carousel & unified search
│   │   │   ├── notifications/       # In-app notifications & notification settings
│   │   │   ├── profile/             # User profile, Edit profile & Reviews screen
│   │   │   └── splash/              # Animated launch splash & session router
│   │   │
│   │   ├── navigation/              # Jetpack Compose Navigation Graph & Typed Routes
│   │   └── ui/                      # Design System
│   │       ├── components/          # Reusable Compose components (GigCard, Nav, Dialogs)
│   │       └── theme/               # Material 3 Color palette, Typography, Elevation
```

### Libraries & Dependencies

| Category | Library | Purpose |
| :--- | :--- | :--- |
| **UI Framework** | Jetpack Compose (BOM) | Declarative UI toolkit |
| **Design System** | Material 3 Components | Modern UI styling & typography |
| **Language** | Kotlin 2.0+ & Coroutines | Asynchronous & reactive programming |
| **State Management** | StateFlow & ViewModel | Reactive unidirectional state |
| **Networking** | Retrofit 2 & OkHttp 4 | Type-safe REST API integration |
| **Realtime** | Socket.IO Client Java | WebSocket bidirectional messaging |
| **Security** | AndroidX Security Crypto | AES-256 encrypted credential storage |
| **Serialization** | Google Gson | JSON parsing and serialization |
| **Compatibility** | Core Library Desugaring | `java.time` support on API 24–25 |

---

## 🚀 Getting Started

### 1. Prerequisites
- **Android Studio**: Ladybug (2024.2.1+) or newer
- **JDK**: Java 17 or higher
- **Android SDK**: Compile SDK `36`, Min SDK `24`

### 2. Clone the Repository
```bash
git clone https://github.com/not-a-hack-er/campus_gig-frontend.git
cd campus_gig-frontend
```

### 3. Environment & Endpoint Configuration
Copy the template configuration file:
```bash
cp keystore.properties.example keystore.properties
```

Edit `keystore.properties` to specify your backend endpoints:
```properties
# Local development on Android Emulator (default host loopback):
BASE_URL="http://10.0.2.2:5000/api/"
SOCKET_URL="http://10.0.2.2:5000"

# Or for Physical Device over Wi-Fi:
# BASE_URL="http://<YOUR_PC_LAN_IP>:5000/api/"
# SOCKET_URL="http://<YOUR_PC_LAN_IP>:5000"
```

> **Note:** `keystore.properties` is protected by `.gitignore` and will never be committed to source control.

### 4. Build and Run
- Open the project directory in **Android Studio**.
- Allow Gradle to sync dependencies.
- Select your target device (Emulator or USB Debugging device) and click **Run (Shift + F10)**.

---

## 🧪 Testing & Build Commands

```bash
# Run Unit Tests
./gradlew testDebugUnitTest

# Assemble Debug APK
./gradlew assembleDebug

# Check Lint and Code Quality
./gradlew lintDebug

# Build Release APK (ProGuard / R8 minified)
./gradlew assembleRelease
```

---

## 🔒 Security & Best Practices

- **Zero Hardcoded Secrets**: Endpoints and keys are injected at compile-time via `BuildConfig` from local properties.
- **R8 / ProGuard Optimization**: Full code minification, obfuscation, and resource shrinking enabled on release builds.
- **Secure Network Traffic**: Cleartext traffic scoped explicitly to local development subnets with production TLS enforcement.

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!
1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'feat: Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  Developed with ❤️ for Students and Campus Freelancers.
</p>
