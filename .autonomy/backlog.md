# Backlog

## High Priority
- [x] Define technical stack (WebView vs Native Components) and architecture.
- [x] Setup initial project structure.
- [x] Add several new development ideas into vision.md

## Medium Priority
- [x] Implement basic WebView wrapper functionality.
- [x] Implement secure credential storage (Android Keystore).
- [x] Fix GitHub Actions build failure — root cause was the missing `gradle.properties` (`android.useAndroidX=true` not set; AGP 8.2 defaults it to false). Fixed by adding `gradle.properties`; CI now also publishes diagnostics on failure.
- [ ] Add a CI step that asserts APK artifacts exist before upload.
- [ ] Add a dependency-verification / lint gate to CI.

## Low Priority
- [x] Implement push notifications.
- [x] Add offline support/caching.
- [ ] Add Network Monitoring & Auto-reconnect handling.
- [ ] Implement Dark Mode support.
