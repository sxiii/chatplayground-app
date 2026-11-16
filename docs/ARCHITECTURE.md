# Architecture: ChatPlayground Android Wrapper

## Tech Stack
- **Framework:** Native Android
- **Language:** Kotlin
- **UI:** Jetpack Compose (where applicable for native elements), WebView (for main app content)
- **Architecture:** MVVM (Model-View-ViewModel)

## Rationale
Using a native WebView wrapper provides the most seamless experience for web-based chat interfaces while allowing for native Android features (like push notifications and secure credential storage) to be implemented later.

## Folder Structure
- `app/`: Native Android application module.
- `docs/`: Project documentation.
