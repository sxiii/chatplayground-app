#!/usr/bin/env python3
"""Fast local pre-push check for the small set of ktlint rules that have
repeatedly broken CI in this repo (trailing newline, trailing whitespace,
tabs in indentation).

It needs no Java, Gradle, or network, so it can be run before every push:

    python3 scripts/check_kotlin_style.py          # check all .kt files
    python3 scripts/check_kotlin_style.py FILE...   # check given files only

Exit code 1 when a violation is found, 0 when clean.
"""

from __future__ import annotations

import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DEFAULT_GLOBS = ("app/src/**/*.kt",)


def collect_files(argv: list[str]) -> list[Path]:
    if argv:
        return [Path(a) for a in argv]
    files: list[Path] = []
    for pattern in DEFAULT_GLOBS:
        files.extend(sorted(ROOT.glob(pattern)))
    return files


def check(path: Path) -> list[str]:
    problems: list[str] = []
    try:
        raw = path.read_bytes()
    except OSError as exc:  # unreadable file is a real problem, report it
        return [f"{path}: cannot read ({exc})"]

    if not raw:
        return [f"{path}: file is empty"]

    if not raw.endswith(b"\n"):
        problems.append("standard:final-newline — file must end with a newline")

    text = raw.decode("utf-8", errors="replace")
    for lineno, line in enumerate(text.splitlines(), start=1):
        if line != line.rstrip():
            problems.append(f"standard:no-trailing-spaces — line {lineno} has trailing whitespace")
        stripped = line.lstrip(" ")
        if stripped.startswith("\t"):
            problems.append(f"standard:indent — line {lineno} indents with a tab")

    return [f"{path}: {p}" for p in problems]


def main(argv: list[str]) -> int:
    files = collect_files(argv)
    if not files:
        print("check_kotlin_style: no .kt files found — nothing to check")
        return 0

    failures: list[str] = []
    for path in files:
        failures.extend(check(path))

    if failures:
        print(f"check_kotlin_style: {len(failures)} violation(s) in {len(files)} file(s)")
        for failure in failures:
            print(f"  {failure}")
        print("\nFix with: python3 scripts/check_kotlin_style.py --fix")
        return 1

    print(f"check_kotlin_style: OK ({len(files)} file(s) checked)")
    return 0


def fix(files: list[Path]) -> int:
    changed = 0
    for path in files:
        raw = path.read_bytes()
        if not raw:
            continue
        text = raw.decode("utf-8", errors="replace")
        new_text = "\n".join(line.rstrip() for line in text.splitlines())
        if not new_text.endswith("\n"):
            new_text += "\n"
        new_raw = new_text.encode("utf-8")
        if new_raw != raw:
            path.write_bytes(new_raw)
            print(f"fixed {path}")
            changed += 1
    print(f"check_kotlin_style --fix: {changed} file(s) rewritten")
    return 0


if __name__ == "__main__":
    args = sys.argv[1:]
    if args and args[0] == "--fix":
        sys.exit(fix(collect_files(args[1:])))
    sys.exit(main(args))