#!/usr/bin/env python3
"""Guard: autonomy progress notes live in exactly one place, ``.autonomy/``.

Previous cycles wrote ``vision.md`` / ``last_run.md`` / ``cycle_summary.md``
into ``autonomy/`` and ``app/autonomy/`` as well as into the canonical
``.autonomy/`` directory. Checkout readers then had several competing "latest"
reports, and a later cycle deleted the strays by hand. This check makes the
mistake fail the build instead of relying on the next agent to notice.

Rules:
  * no directory named ``autonomy`` may exist outside the canonical
    ``.autonomy/`` at the repository root
    (``.autonomy`` is a different name, so it never matches the walk);
  * ``.autonomy/`` itself must exist, so the canonical state directory cannot
    be deleted or moved without the build noticing.

Exit codes: 0 = canonical, 1 = violation (message on stdout).
"""

from __future__ import annotations

import pathlib
import sys

CANONICAL = pathlib.Path(".autonomy")

# Build output and tool caches can contain a directory called "autonomy" for
# unrelated reasons (e.g. a dependency's sample project); they are not part of
# the source tree and must not fail the guard.
SKIP_DIR_NAMES = {".git", ".gradle", ".idea", ".cxx", "build", "tmp_artifacts"}


def find_stray_autonomy_dirs(root: pathlib.Path) -> list[pathlib.Path]:
    """Return every ``autonomy`` directory that is not the canonical one."""
    findings: list[pathlib.Path] = []
    for path in sorted(root.rglob("autonomy")):
        if not path.is_dir():
            continue
        if any(part in SKIP_DIR_NAMES for part in path.parts):
            continue
        findings.append(path)
    return findings


def main() -> int:
    problems: list[str] = []

    if not CANONICAL.is_dir():
        problems.append(
            f"{CANONICAL}: missing — canonical autonomy state lives here "
            "(vision.md, last_run.md, cycle_summary.md)"
        )

    for stray in find_stray_autonomy_dirs(pathlib.Path(".")):
        problems.append(
            f"{stray}: stray autonomy notes directory — the only allowed "
            f"location is {CANONICAL}/ at the repository root"
        )

    if problems:
        print("Autonomy notes are not stored in the canonical location:")
        for problem in problems:
            print(f"  - {problem}")
        print(f"\nFix: move the notes into {CANONICAL}/ and delete the stray directory.")
        return 1

    print(f"OK: autonomy notes live only in {CANONICAL}/ at the repository root.")
    return 0


if __name__ == "__main__":
    sys.exit(main())