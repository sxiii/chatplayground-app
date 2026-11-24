# Last Run (2026-11-24 05:58 CET)

## Baseline
- CI run `45327633876` (HEAD `6426d6f`, the captive-portal cycle) **failed** on `Run lintKotlin`:
  - `NetworkMonitorCapabilityChangeTest.kt:47` — `standard:function-signature` ("First line of body expression fits on same line as function signature").
  - `NetworkMonitorCapabilityChangeTest.kt:158` — `standard:final-newline` ("File must end with a newline").
- All prior steps (debug APK build, unit tests, lint, Robolectric SDK guard) were green.
- Canonical autonomy state: `.autonomy/`; no stray `autonomy/` dirs present.

## What changed (this cycle)
1. **Fixed the two ktlint violations in `NetworkMonitorCapabilityChangeTest.kt`**:
   - Collapsed `registeredCallback()` signature + single-line body onto one line (function-signature rule).
   - Added final trailing newline.
2. **Cleaned `.gitignore`**: re-added `captures/`, `.cxx/` (previously removed in error), added `local.properties` (CI writes its own `sdk.dir`); untracked the committed `local.properties` (`git rm --cached`) so CI's per-run copy doesn't conflict.
3. **Added `ci_check_autonomy_dirs.py` CI guard**: fails when `autonomy/` or `app/autonomy/` exists outside canonical `.autonomy/`, or when `.autonomy/` itself is missing; wired into `.github/workflows/build.yml` right after the Robolectric SDK guard, before any Gradle work.
4. **Documented the guard** in `docs/FUNCTIONS.md`.

## Verification (local)
- `ci_check_robolectric_sdk.py` → OK.
- `ci_check_autonomy_dirs.py` → passes with no stray dirs; fails on `autonomy/` and `app/autonomy/`; passes again once removed.
- `local.properties` untracked; `git check-ignore` confirms it is now excluded.
- Android build/local `./gradlew` cannot run here (no Java on this host) — verification is via GitHub workers only, per AUTONOMY.md.

## Next actions (ranked)
1. Wait for CI run `45327633876` + this cycle's push result; if green, mark vision item 49 DONE in `.autonomy/vision.md`.
2. Item 50: merge the overlapping offline tests (`NetworkMonitorTest.reportsOfflineWhenNoNetworkIsAvailable` / `NetworkMonitorLoadErrorTest`) into named cases.
3. Item 51 / 52: already DONE this cycle per last report (capability-change + onUnavailable) — confirm they remain marked DONE.