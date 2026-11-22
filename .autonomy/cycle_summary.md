# Autonomous Development Cycle Summary (2026-11-22, Custom Tabs)

## Baseline check
`git log` head `07f5e3e`; GitHub Actions run `44976992088` = success (5m17s).
GitHub status page operational. So CI was green before any change in this cycle —
the previous cycle's Robolectric fixes are confirmed on the runner.

## Task selection
From `.autonomy/vision.md` the highest-value open item with a real user impact was
**#2 Custom Tabs for external links** (the HIGH item is test-infrastructure
consistency; it is queued next). Chosen scope: user-clicked off-domain links leave
the WebView through a Custom Tab instead of hijacking the app's single WebView.

## TDD in a CI-only build environment
Project policy forbids a local Java/Gradle harness, so RED/GREEN were observed on
GitHub workers:

| Step | Commit | CI run | Result |
|---|---|---|---|
| RED (tests only) | `ae00434` | `44978726869` | failure — `Unresolved reference: LinkRouter`, `externalLinkLauncher` |
| GREEN (implementation) | `4cfbfd7` | `44979464647` | success — 19 suites, all passed |

Design refinement during GREEN: the first spec routed *every* off-domain `http(s)`
navigation to a Custom Tab, which would break in-WebView OAuth/form redirects
(Custom Tabs do not share the WebView cookie jar). The implemented rule is
gesture-gated — `request.hasGesture()` — with a dedicated test
(`offDomainRedirectWithoutUserGestureStaysInWebView`) pinning that behaviour.

## Diff review
- New: `LinkRouter.kt`, `LinkRouterTest.kt`.
- Changed: `MainActivity` (routing + seam + imports), `MainActivityTest`
  (`requestFor(url, hasGesture)`, 3 new cases), `app/build.gradle` (one
  dependency), `docs/FUNCTIONS.md` (documented before implementation).
- No test weakened, no unrelated formatting churn, no credential changes.

## Verification
CI run `44979464647`: APK debug + release built, JaCoCo exec uploaded, lint passed,
`Check Unit Test Results` reported 19 XML suites with zero failures/errors.

## Carried forward
`.autonomy/vision.md` now lists six ranked items; the next cycle should start with
the HIGH Robolectric SDK-consistency task (`robolectric.properties`).

---

# Autonomous Development Cycle Summary (2026-11-22, Robolectric SDK consistency)

## Baseline check
`git log` head `4cfbfd7`; GitHub Actions run `44979464647` = success (8m34s). GitHub status page operational — no incidents on 2026-11-22. CI was green before this cycle; Custom Tabs cycle is closed and all 19 test suites passed on the runner.

## Task selection
From `.autonomy/vision.md` the highest-value open item was **#1 Robolectric SDK consistency (HIGH)** — seven suites inherited Robolectric's default SDK while others pinned 26 or 30, reintroducing `NoClassDefFoundError`/`KeyStoreException` after dependency bumps. Acceptance: `app/src/test/resources/robolectric.properties` (`sdk=26`) as the single source of truth, all per-class pins removed, plus a CI guard.

## TDD in a CI-only build environment
Project policy forbids a local Java/Gradle harness, so RED/GREEN were verified with the guard itself (no compile needed — it's a pure config check):

| Step | Commit | Result |
|---|---|---|
| RED — guard runs and fails | working tree | exit 1: 11 per-class pins + missing/wrong SDK all detected |
| Guard edge cases | scratch copy | rejects `sdk=30`; rejects `@Config(minSdk = 24)`; passes clean state |
| GREEN — implementation | `7fc2b07` | properties file created; 11 pins removed; CI step added; docs updated |

## Diff review
- New: `app/src/test/resources/robolectric.properties` (`sdk=26`); `ci_check_robolectric_sdk.py`.
- Changed: `.github/workflows/build.yml` (add `Verify Robolectric SDK Consistency` after `Check Unit Test Results`), `docs/FUNCTIONS.md` (§1), 11 test files (drop `@Config` + unused import), `MainActivityLoadingIndicatorTest.kt` (doc comment).
- No test weakened, no unrelated churn, no credential changes.

## Verification
- `ci_check_robolectric_sdk.py`: OK — `sdk=26` pinned; no per-class SDK overrides.
- Pushed `4cfbfd7..7fc2b07` to `origin/main`; CI run `44982364135` in progress at cutover.

## Carried forward
`.autonomy/vision.md` lists six open items; top is ktlint enforcement (§4 documents `org.jmailen.gradle.kotlinter` 4.3.0 but neither `app/build.gradle` nor the workflow contains it) plus two new MEDIUM items (WebView back-button history navigation; Mixed Content / HTTPS enforcement).

## Next Action
Wait for CI run `44982364135` to finish; if green, take the ktlint item next.
