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