# Last Run (2026-11-22 04:20 CET)

## Baseline
- Previous push `07f5e3e` built green on CI (run `44976992088`, 5m17s): APK + all
  unit tests passed, so the Robolectric fixes from the prior cycle are confirmed
  on the runner, not just locally reasoned about.

## Cycle Outcome
Delivered **Custom Tabs routing for off-domain links** (vision item 2, MEDIUM),
test-first:

1. **RED** (run `44978726869`, failed): tests-only commit specifying
   `LinkRouter` (`LinkRouterTest`, 9 cases) and `MainActivity.externalLinkLauncher`
   (3 new `MainActivityTest` cases). CI failed at `compileDebugUnitTestKotlin`
   with `Unresolved reference: LinkRouter` / `externalLinkLauncher`.
2. **GREEN** (run `44979464647`, success): implemented
   - `app/src/main/java/com/chatplayground/app/utils/LinkRouter.kt` —
     `isInAppUrl`, `isExternalWebUrl`, `isExternalScheme`, parsed with
     `java.net.URI` (same host rules as `DeepLinkResolver`; look-alike hosts and
     subdomains are external).
   - `MainActivity.shouldOverrideUrlLoading` → `handleUrl(url)`, gated on
     `request.hasGesture()` for web URLs so OAuth/form redirects stay in the
     WebView; non-web schemes keep the old `ACTION_VIEW` behaviour.
   - `MainActivity.externalLinkLauncher: (Uri) -> Unit` — injectable seam,
     default `CustomTabsIntent` with `ACTION_VIEW` fallback and a toast when
     nothing can handle the link.
   - `androidx.browser:browser:1.8.0` dependency.
   - `docs/FUNCTIONS.md` documented first (Custom Tabs bullet + `LinkRouter`
     section), per the project rule.
3. `.autonomy/vision.md` re-ranked: item 2 closed, six evidence-backed
   suggestions recorded (top: Robolectric SDK consistency, HIGH).

## Evidence
- CI run `44979464647`: `Build Debug APK`, `Run Unit Tests`,
  `Verify Dependencies`, JaCoCo exec upload, `Run Lint`, `Build Release APK` all
  green; `Check Unit Test Results` — "Found 19 JUnit XML result file(s) …
  All JUnit test suites passed"; artifacts `chatplayground-apk-outputs`
  (15.4 MB) and `jacoco-exec-data` (36 KB).

## Next Action
Take the HIGH item: pin Robolectric's SDK once via
`app/src/test/resources/robolectric.properties` (`sdk=26`), drop contradictory
per-class `@Config(sdk = …)` pins, and prove no suite depends on the default SDK
(full-suite CI run must stay green, and a deliberately wrong pin must fail).