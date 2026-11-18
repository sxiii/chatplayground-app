# Project Vision: ChatPlayground Android Wrapper

The objective of this project is to create a robust, secure, and user-friendly Android application wrapper for the ChatPlayground web application.

## Key Goals
1. Provide a native-like experience for interacting with ChatPlayground on Android.
2. Ensure secure handling of session data and credentials.
3. Support offline functionality where applicable.
4. Maintain high performance and responsiveness.
5. Iterate on ideas and improvements for the app.
6. Enhance UI/UX based on user feedback.
7. Integrate analytics for performance monitoring.

- [ ] Add CI step to verify APK artifacts exist before upload (implemented in workflow).
- [x] Fix GitHub Actions build failure (Gradle task failing after Android SDK setup; verbose logs now enabled via `--info --debug` to pinpoint the root cause).
- [ ] Add Gradle build cache (e.g., `actions/cache`) to the CI pipeline to speed up builds.
- [ ] Implement user feedback mechanism for UI/UX improvements.
- [ ] Add dependency verification step to the CI pipeline to ensure security.
- [ ] Implement Dark Mode support.
- [ ] Add a settings screen for toggling features.
- [ ] Implement Biometric Authentication for enhanced security.
- [ ] Add Network Monitoring & Auto-reconnect handling.
- [ ] Implement Screen Reader accessibility improvements for the WebView content.
- [ ] Add basic performance profiling for initial app load.
- [ ] Add automated UI integration tests using Espresso.
- [ ] Migrate `actions/setup-java` from v4 to v5 (v4 is deprecated and will stop receiving updates).
- [ ] Pin and bump the AGP / Gradle / Kotlin versions together in a single verified step (currently AGP 8.2.0 + Gradle 8.5 + Kotlin 1.9.0).
- [ ] Add an explicit CI step that asserts the generated APK artifacts exist before upload (fail fast with a clear message).
- [ ] Add `actions/cache` for `android-sdk` to further speed up builds.
- [ ] Run `./gradlew lint` in CI and gate on lint results.
- [ ] Add unit tests for SecureStorage implementation (covers encryption, retrieval, and error handling).
- [x] Upgrade `androidx.webkit:webkit` to latest version.
- [ ] Investigate alternative CI failure causes if upgrade fails.
