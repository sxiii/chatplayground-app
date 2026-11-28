# Last Run (2026-11-28, run 17 — immersive-mode adapter and menu)

## Baseline
- GitHub status API: **All Systems Operational**.
- Latest worker run `46099049526` completed **successfully** on `main` at `62e8dcb`; prior repair run `46098362338` also passed.
- Working tree was clean before implementation.

## Task Chosen
- Vision item 236/237: finish the immersive-mode cycle by wiring the persisted state to Android window insets and the application menu.

## What Changed
- Documented the adapter and menu contract in `docs/FUNCTIONS.md` before implementation.
- Added `ImmersiveModeWindowAdapter`, using `WindowInsetsControllerCompat` to hide/show system bars with transient bars available by edge swipe.
- `MainActivity` restores immersive mode before the first WebView load, adds the menu item, persists toggles, applies the adapter immediately, and updates the title.
- Added menu/state regression coverage and reset immersive singleton state in `SingletonCleanupRule`.
- Added ranked follow-up suggestions for real-window instrumentation coverage and preference-key consolidation.

## Verification
- `python3 scripts/check_kotlin_style.py` — PASSED (76 Kotlin files).
- `python3 scripts/test_ci_checks.py` — PASSED (18 tests).
- `git diff --check` — PASSED.
- Gradle/JVM tests were not run locally by project policy; GitHub worker is required.
- A local `pytest` attempt was unavailable (`No module named pytest`); unrelated to the Android worker path.

## Next Actions
- Push this cycle to `main`; verify the worker build before selecting the next task.
- If green, consider item 238 instrumentation coverage for real system-bar visibility.
