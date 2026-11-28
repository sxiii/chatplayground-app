# Last Run (2026-11-28, run 19 — immersive instrumentation coverage and repair)

## Baseline
- GitHub status page: **All Systems Operational**; latest prior run `46103137179` completed successfully.
- Repository was clean at `main` before this cycle.

## Task Chosen
- Vision item 241: add instrumentation coverage for immersive system-bar visibility.

## What Changed
- Documented the instrumentation contract in `docs/FUNCTIONS.md` before implementation.
- Added `ImmersiveModeWindowAdapterInstrumentedTest`, which launches a real `MainActivity` window and verifies immersive application hides system bars and non-immersive application restores them.
- Added `androidx.test:core` to the androidTest configuration for `ActivityScenario`.
- First push was commit `770c9fb`. Worker run `46105266717` reached compilation and failed only at `lintKotlinAndroidTest` because three helper signatures violated `standard:function-signature`.
- Reformatted those helper signatures; no production behavior changed.

## Verification
- `python3 scripts/check_kotlin_style.py app/src/androidTest/java/com/chatplayground/app/utils/ImmersiveModeWindowAdapterInstrumentedTest.kt` — PASSED before and after repair.
- `git diff --check` — PASSED before and after repair.
- GitHub diagnostics for `46105266717` identified the exact six formatting messages; no compile or test failure was reported.
- Gradle was not run locally by project policy.

## Next Actions
- Commit and push the formatting repair directly to `main`, then verify the replacement worker run.
- If green, consider preference-key constants or per-site WebStorage UI.
