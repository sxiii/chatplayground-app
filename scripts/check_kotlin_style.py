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

MAX_LINE_LENGTH = 140


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
    lines = text.splitlines()
    for lineno, line in enumerate(lines, start=1):
        if line != line.rstrip():
            problems.append(f"standard:no-trailing-spaces — line {lineno} has trailing whitespace")
        stripped = line.lstrip(" ")
        if stripped.startswith("\t"):
            problems.append(f"standard:indent — line {lineno} indents with a tab")

    problems.extend(
        f"standard:multiline-expression-wrapping — line {ln}: {l}"
        for ln, l in _multiline_expression_wrapping_violations(lines)
    )

    for lineno, line in enumerate(lines, start=1):
        if len(line) > MAX_LINE_LENGTH:
            problems.append(
                f"standard:max-line-length — line {lineno} is {len(line)} chars "
                f"(max {MAX_LINE_LENGTH})"
            )

    return [f"{path}: {p}" for p in problems]


def _multiline_expression_wrapping_violations(lines: list[str]) -> list[tuple[int, str]]:
    """Flag assignments whose RHS is a multiline call starting mid-line.

    ktlint `standard:multiline-expression-wrapping`: when an expression spans
    multiple lines it must start on a new line, e.g.

        BAD:  val urls = listOf(
                  "https://example.com/a",
              )
        GOOD: val urls =
                  listOf(
                      "https://example.com/a",
                  )

    A plain-JVM heuristic: track paren/brace/bracket depth; when depth leaves
    zero on a line that also contains `=` before the opening delimiter and the
    expression closes on a later line, the call started mid-line — violation.
    """
    violations: list[tuple[int, str]] = []
    depth = 0
    in_expr = False
    expr_start = 0
    expr_start_line = ""
    expr_has_assign_before = False
    for lineno, raw in enumerate(lines, start=1):
        line = raw.rstrip()
        stripped = line.strip()
        if not stripped or stripped.startswith("//"):
            continue
        prev = depth
        for ch in stripped:
            if ch in "({[":
                depth += 1
            elif ch in ")}]":
                depth -= 1
        if not in_expr and prev == 0 and depth > 0:
            # expression begins on this line
            in_expr = True
            expr_start = lineno
            expr_start_line = line
            rhs_before_paren = stripped.split("(", 1)[0]
            expr_has_assign_before = "=" in rhs_before_paren
        elif in_expr and depth == 0:
            if lineno > expr_start and expr_has_assign_before:
                violations.append((expr_start, expr_start_line))
            in_expr = False
            expr_start = 0
            expr_start_line = ""
            expr_has_assign_before = False
    return violations


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