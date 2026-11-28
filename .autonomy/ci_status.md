# CI Status
## Current Run
- **CI status**: Green at HEAD `17f3d85` (commit `test(data): verify clear-all ordering`). Run `46092737887` completed **success** on `main`; build and release-tag jobs passed.
- No pending worker errors.

## Recent CI Runs
| Run ID | Status | Notes |
|--------|--------|-------|
| 46092737887 | success | push `17f3d85` — DataClearer clearing-order seam and regression test |
| 46089528072 | success | docs sync at `b8fa417` |
| 46088833666 | success | AppLock reset fix at `ec55d9d` |
| 46087981612 | failure | unused import; fixed in later run |
| 46087329602 | failure | missing AppLock import; fixed in later run |

## CI Guard Status (local verification)
- `scripts/check_kotlin_style.py`: PASSED (all 73 files).
- `scripts/test_ci_checks.py`: PASSED (18 tests, all OK).
- `git diff --check`: PASSED.

## Next Actions
- Open work: item 228 (per-site WebStorage UI), item 231/60 (immersive mode seam-first implementation), item 226 (NetworkMonitor rapid subscribe/unsubscribe tests), item 55 (CI dependency caching/retry), and item 234 (coverage guard).
