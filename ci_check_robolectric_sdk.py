#!/usr/bin/env python3
"""Guard: Robolectric's Android SDK is configured in exactly one place.

Robolectric reads ``app/src/test/resources/robolectric.properties`` for every
test class. Per-class ``@Config(sdk = ...)`` annotations silently override that
single source of truth, which is how the 2026-11-22 ``NoClassDefFoundError`` /
``KeyStoreException`` failures crept back in after a dependency bump: seven test
classes inherited Robolectric's default SDK while others pinned 26 or 30.

This check fails the build when:
  * the properties file is missing, or
  * its ``sdk`` key is not a single pinned value equal to the expected SDK, or
  * any test source under ``app/src`` pins an SDK via ``@Config``.

Exit codes: 0 = consistent, 1 = violation (message on stdout).
"""

from __future__ import annotations

import pathlib
import re
import sys

EXPECTED_SDK = "26"
PROPERTIES = pathlib.Path("app/src/test/resources/robolectric.properties")
TEST_SOURCES = pathlib.Path("app/src")

CONFIG_SDK_RE = re.compile(r"@Config\s*\(")


def _read_properties(path: pathlib.Path) -> dict[str, str]:
    values: dict[str, str] = {}
    for raw in path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        if "=" not in line:
            continue
        key, value = line.split("=", 1)
        values[key.strip()] = value.strip()
    return values


def _scan_config_sdk_overrides(root: pathlib.Path) -> list[str]:
    """Return ``path:line: snippet`` for every test class pinning an SDK."""
    findings: list[str] = []
    if not root.is_dir():
        return [f"{root}: missing test source directory"]
    # Annotations can wrap onto the next line, so tolerate whitespace/newlines
    # between '@Config(' and the first key.
    annotation_re = re.compile(
        r"@Config\s*\(\s*(?:[^)]*?\b(?:sdk|minSdk|maxSdk)\s*=)", re.DOTALL
    )
    for path in sorted(root.rglob("*.kt")):
        text = path.read_text(encoding="utf-8")
        for match in annotation_re.finditer(text):
            line_no = text.count("\n", 0, match.start()) + 1
            snippet = " ".join(match.group(0).split())
            findings.append(f"{path}:{line_no}: {snippet}")
    return findings


def main() -> int:
    problems: list[str] = []

    if not PROPERTIES.is_file():
        problems.append(
            f"{PROPERTIES}: missing — Robolectric would fall back to its "
            f"default SDK instead of the pinned {EXPECTED_SDK}"
        )
    else:
        values = _read_properties(PROPERTIES)
        sdk = values.get("sdk")
        if sdk is None:
            problems.append(f"{PROPERTIES}: no 'sdk' key")
        elif sdk != EXPECTED_SDK:
            problems.append(
                f"{PROPERTIES}: sdk={sdk!r}, expected exactly {EXPECTED_SDK!r}"
            )

    problems.extend(_scan_config_sdk_overrides(TEST_SOURCES))

    if problems:
        print("Robolectric SDK configuration is not centralized:")
        for problem in problems:
            print(f"  - {problem}")
        print(
            f"\nFix: keep 'sdk={EXPECTED_SDK}' in {PROPERTIES} and delete "
            "per-class @Config(sdk/minSdk/maxSdk) pins."
        )
        return 1

    print(
        f"OK: Robolectric SDK pinned to {EXPECTED_SDK} in {PROPERTIES}; "
        "no per-class SDK overrides."
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())