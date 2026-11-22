# Autonomy Vision

- Added improvements: improved CI/CD pipeline, automated testing, biometric authentication for secure storage, and documentation.
- Tasks: implement vision.md improvements, update README, push changes.

## Cycle 2026-11-22 (04:58 UTC) — Back-button history navigation

Ranked open tasks this cycle (evidence in `.autonomy/vision.md`):

1. **[MEDIUM, this cycle] WebView back-button history navigation** — system Back closes
   the activity and discards in-WebView history. Acceptance: an `OnBackPressedCallback`
   calls `webView.goBack()` while `canGoBack()` is true, otherwise falls through to
   activity finish; two `MainActivityTest` cases cover both branches.
2. **[MEDIUM] Mixed Content / HTTPS enforcement** — `WebViewSettings` still allows mixed
   content; acceptance: `MIXED_CONTENT_NEVER_ALLOW` plus a unit test asserting the setting.
3. **[MEDIUM] ktlint enforcement gap** — `docs/FUNCTIONS.md` §4 claims
   `org.jmailen.gradle.kotlinter` 4.3.0 + a CI `ktlintCheck` step; neither exists.
   Deferred: the required formatting pass can only run through Gradle, and this project
   forbids local Java/Gradle harnesses, so it needs a CI-side design (formatting job that
   fails with a diff artifact) before it can land honestly.
4. **[MEDIUM] JaCoCo report disabled** — enable `xml`/`html` reports and upload
   `app/build/reports/jacoco/**`.
5. **[LOW] README.md** is a single line; document the GitHub-native build requirement and
   where CI status/artifacts live.

## Next steps
- Verify the back-navigation cycle on CI, then record the outcome in `.autonomy/`.
- Then pick item 2 (mixed content) — smallest testable behavior after this one.
- Implement Android Custom Tabs for external links. (DONE 2026-11-22)
- Configure Android Intent Filters for deep linking. (DONE 2026-11-22)
- Add `getOfflineUrl` utility to `UrlProvider` with TDD. (DONE — offline page handled in
  `onReceivedError`; re-scope only if a test seam is still wanted)

## Improvement Suggestions (2026-11-22)

- **[MEDIUM] Enable JaCoCo xml/html coverage reports and upload them in CI** — `jacocoTestReport` writes only raw `.exec` data (`xml.required = html.required = false`). Enable `xml` and `html` reports, upload `app/build/reports/jacoco/**` as a workflow artifact, and fail the build when the report is absent.
- **[MEDIUM] ktlint enforcement gap** — `docs/FUNCTIONS.md` §5 claims `org.jmailen.gradle.kotlinter` 4.3.0 + a CI `ktlintCheck` step; neither exists. Either implement it (CI-side formatting job with diff artifact, since local Gradle is forbidden) or delete the section.
- **[LOW] Misleading CI step name** — `Upload JUnit Test Results` duplicates `Collect JUnit Test Results`; rename or drop.
- **[MEDIUM] Dead code in test files after refactors breaks CI compile** — `MainActivitySubResourceErrorTest.kt` kept `TestWebResourceRequest`/`TestWebResourceError` helper classes after the sub-resource error test was rewritten to call `activity.handleWebViewError()` directly. `TestWebResourceError()` is package-private, so CI `compileDebugUnitTestKotlin` fails. Acceptance: when a test refactor removes the only caller of a helper class, delete the helper and its imports; CI compile must be green before merge.
- **[LOW] Record failing CI run ID and root cause in `.autonomy/`** — cycles starting from a broken baseline should note the failing CI run ID (e.g. `45021344564`) and the worker error so the next cycle begins from the failure, not an assumed-green state. Acceptance: `last_run.md`/`cycle_summary.md` record the CI run ID and compile error text.

## Improvement Suggestions (2026-11-22, after the sub-resource error cycle)

- **[MEDIUM] Keep test-invoked methods on the owning class, not inside anonymous listeners** — this cycle's CI failure was `Unresolved reference: handleWebViewError` (runs `45024039219`, `45025886373`): `handleWebViewError` was declared inside the anonymous `WebViewClient` assigned to `webView.webViewClient`, so a unit test calling `activity.handleWebViewError(...)` could never resolve — changing its visibility to `internal` did not help. Fix (commit `709aaaa`): move it onto `MainActivity` as `internal` and have both `onReceivedError` overloads delegate via `this@MainActivity.handleWebViewError(...)`. Acceptance: any method a test calls through the activity instance must be declared on the activity, not nested in an anonymous inner object; CI compile must be green before merge. Verified GREEN: CI run `45026480870` success.
- **[LOW] Next cycle pick the `NetworkMonitor` UI-thread issue** — `MainActivity.onCreate` passes `observe { isConnected -> if (isConnected && webView.url == "file:///android_asset/offline.html") … }`; the callback runs on the connectivity agent thread, so `webView.url` is read off the UI thread. Acceptance: move the whole callback body into `runOnUiThread` (or a main-thread `Handler`) and cover it with a test driving `onAvailable` from a non-main thread.
- **[LOW] Docs section numbering collides** — `docs/FUNCTIONS.md` has two `## 4.` headings (`4. WebView UI & Loading` and `4. Code Formatting Enforcement (CI)`), so `§4` references are ambiguous (and the formatting section is the one whose ktlint claims are unimplemented). Acceptance: renumber the formatting section to 5.
- **[MEDIUM] Offline-page fallback tests now call `activity.handleWebViewError(...)` directly** — `MainActivitySubResourceErrorTest` drives the extracted method, which pins behaviour but bypasses the `WebViewClient` dispatch path (the deprecated `onReceivedError` still constructs a synthetic `WebResourceRequest`). Acceptance: add one case that drives `webViewClient?.onReceivedError(webView, request, error)` for both main-frame and sub-resource requests so the dispatch-to-handler wiring is covered end to end.