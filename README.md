# CampusVault Android app (CampusGig client)

An Android client for the campus gig marketplace backed by a companion API (currently in a private repository). The repository name retains `campusgig_frontend`; the current app label in `strings.xml` is **CampusVault**.

Students can browse and post gigs, apply with proposals, view profiles, communicate, and receive updates. The project uses Kotlin, Jetpack Compose, Retrofit/OkHttp, Socket.IO, and Firebase messaging. Provider-dependent features require a configured backend and credentials.

## Screenshots

<p>
  <img src="play-store-assets/phone-screenshots/01-discover-gigs.png" alt="Discover gigs" width="180" />
  <img src="play-store-assets/phone-screenshots/02-gig-marketplace.png" alt="Gig marketplace" width="180" />
  <img src="play-store-assets/phone-screenshots/03-build-your-profile.png" alt="Profile" width="180" />
  <img src="play-store-assets/phone-screenshots/04-post-a-gig.png" alt="Post a gig" width="180" />
</p>

These are checked-in promotional screenshots. For a current hands-on review, build the app and verify the live flow against your configured backend.

## Start a technical review here

| Concern | Source |
| --- | --- |
| App startup and dependency wiring | [`CampusGigApplication.kt`](app/src/main/java/com/abpvt/campusgig_frontend/CampusGigApplication.kt) |
| REST contract and authentication | [`core/network`](app/src/main/java/com/abpvt/campusgig_frontend/core/network) |
| Repository/data boundary | [`data/repository`](app/src/main/java/com/abpvt/campusgig_frontend/data/repository) |
| Gig UI | [`features/gigs`](app/src/main/java/com/abpvt/campusgig_frontend/features/gigs) |
| Real-time messaging | [`features/chat/SocketManager.kt`](app/src/main/java/com/abpvt/campusgig_frontend/features/chat/SocketManager.kt) |
| Unit tests | [`app/src/test`](app/src/test) |

## Build locally

Requirements: Android Studio, JDK 17+, and Android SDK 36. Minimum supported device API is 24.

1. Clone the repository and open it in Android Studio.
2. Review [`keystore.properties.example`](keystore.properties.example). Create an untracked `keystore.properties` only for the endpoint/auth settings you need; use real signing details only when building a release.
3. Set `BASE_URL` (including `/api/`) and `SOCKET_URL` to a reachable backend. The emulator can reach a host server through `10.0.2.2`; physical devices cannot use that address.
4. Add your own Firebase configuration and OAuth/Clerk values when testing those integrations. Do not commit local credentials or signing files.
5. Run the `app` configuration in Android Studio or build from the command line:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

On Windows, use `gradlew.bat`. The debug unit-test task completed successfully in the local checkout on 13 October 2026. This does not imply that device, backend, payment, or release-signing flows have all been verified.

## Architecture at a glance

The `features/` packages contain Compose screens and view models. Repositories in `data/` mediate network access through the API layer; the Socket.IO client handles live chat events. The app also includes push-notification and payment integration code. Review the corresponding backend routes and sandbox configuration before treating a workflow as end-to-end complete.

## Responsible release notes

No store-download or production-usage metric is claimed here. Release bundles in the local checkout are build artifacts, not proof that the current source is approved or deployed. Before publishing, verify signing, backend URLs, provider settings, privacy disclosures, account deletion, and payment boundaries for the exact release build.
