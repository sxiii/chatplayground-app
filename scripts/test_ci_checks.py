#!/usr/bin/env python3
"""Self-tests for the repository's CI guard scripts.

Every guard in this repo is exit-code-only:
``ci_check_tests.py``, ``ci_check_robolectric_sdk.py``,
``ci_check_autonomy_dirs.py`` and ``scripts/check_kotlin_style.py``.

That makes a silent regression indistinguishable from a clean repo: a guard
that always returns 0 looks exactly like a guard that finds nothing wrong.
These self-tests close that gap. For each guard they build a throwaway
directory, plant a *known-bad* fixture, run the guard, and assert it exits
non-zero. A matching *known-good* fixture is asserted to exit 0 so the guard is
proven to discriminate - not merely to always fail (which a
``sys.exit(1)``-on-any-input stub would also satisfy).

Stdlib only; no Java, Gradle, network, or Android SDK, so it runs in the same
second as the other checks and can be run locally before a push:

    python3 scripts/test_ci_checks.py

Exit code 0 when every guard behaves, 1 when a guard stopped detecting its
defect (or started rejecting valid input).
"""

from __future__ import annotations

import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
PY = sys.executable

CI_CHECK_TESTS = REPO_ROOT / "ci_check_tests.py"
CI_CHECK_ROBOLECTRIC = REPO_ROOT / "ci_check_robolectric_sdk.py"
CI_CHECK_AUTONOMY = REPO_ROOT / "ci_check_autonomy_dirs.py"
CHECK_KOTLIN_STYLE = REPO_ROOT / "scripts" / "check_kotlin_style.py"


def run_guard(script: Path, cwd: Path, *args: str) -> subprocess.CompletedProcess:
    """Run *script* with *cwd* as the working directory (guards read CWD)."""
    return subprocess.run(
        [PY, str(script), *args],
        cwd=str(cwd),
        capture_output=True,
        text=True,
    )


def write_file(root: Path, relpath: str, content: str) -> Path:
    path = root / relpath
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")
    return path


class CiCheckTestsGuardTest(unittest.TestCase):
    """``ci_check_tests.py`` must fail when no results or failures exist."""

    def test_fails_when_junit_results_directory_is_missing(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            result = run_guard(CI_CHECK_TESTS, Path(tmp))
        self.assertNotEqual(result.returncode, 0, result.stdout)
        self.assertIn("No JUnit XML", result.stdout)

    def test_fails_when_junit_results_directory_is_empty(self) -> None:
        """The guard should fail when junit-results/ exists but contains no XML files."""
        with tempfile.TemporaryDirectory() as tmp:
            write_file(Path(tmp), "junit-results/.gitkeep", "")
            result = run_guard(CI_CHECK_TESTS, Path(tmp))
        self.assertNotEqual(result.returncode, 0, result.stdout)
        self.assertIn("No JUnit XML", result.stdout)

    def test_fails_when_a_suite_reports_a_failure(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            write_file(
                Path(tmp),
                "junit-results/TEST-Foo.xml",
                '<testsuite name="Foo" tests="1" failures="1" errors="0"></testsuite>',
            )
            result = run_guard(CI_CHECK_TESTS, Path(tmp))
        self.assertNotEqual(result.returncode, 0, result.stdout)
        self.assertIn("Foo", result.stdout)

    def test_fails_when_a_suite_reports_an_error(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            write_file(
                Path(tmp),
                "junit-results/TEST-Foo.xml",
                '<testsuite name="Foo" tests="1" failures="0" errors="1"></testsuite>',
            )
            result = run_guard(CI_CHECK_TESTS, Path(tmp))
        self.assertNotEqual(result.returncode, 0, result.stdout)

    def test_passes_with_a_clean_passing_suite(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            write_file(
                Path(tmp),
                "junit-results/TEST-Foo.xml",
                '<testsuite name="Foo" tests="1" failures="0" errors="0"><testcase name="works"/></testsuite>',
            )
            result = run_guard(CI_CHECK_TESTS, Path(tmp))
        self.assertEqual(result.returncode, 0, result.stdout)


class RobolectricSdkGuardTest(unittest.TestCase):
    """``ci_check_robolectric_sdk.py`` must reject un-centralised SDK setup."""

    def _properties(self, root: Path, sdk: str) -> None:
        write_file(
            root, "app/src/test/resources/robolectric.properties", f"sdk={sdk}\n"
        )

    def test_fails_when_properties_file_is_missing(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            write_file(Path(tmp), "app/src/main/java/Foo.kt", "class Foo\n")
            result = run_guard(CI_CHECK_ROBOLECTRIC, Path(tmp))
        self.assertNotEqual(result.returncode, 0, result.stdout)
        self.assertIn("missing", result.stdout)

    def test_fails_when_properties_sdk_is_not_the_expected_value(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            self._properties(Path(tmp), "30")
            write_file(Path(tmp), "app/src/main/java/Foo.kt", "class Foo\n")
            result = run_guard(CI_CHECK_ROBOLECTRIC, Path(tmp))
        self.assertNotEqual(result.returncode, 0, result.stdout)
        self.assertIn("expected exactly", result.stdout)

    def test_fails_when_a_test_class_pins_an_sdk(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            self._properties(Path(tmp), "26")
            write_file(
                Path(tmp),
                "app/src/test/java/FooTest.kt",
                "@Config(sdk = [34])\nclass FooTest\n",
            )
            result = run_guard(CI_CHECK_ROBOLECTRIC, Path(tmp))
        self.assertNotEqual(result.returncode, 0, result.stdout)
        self.assertIn("FooTest.kt", result.stdout)

    def test_passes_when_centralised(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            self._properties(Path(tmp), "26")
            write_file(Path(tmp), "app/src/test/java/FooTest.kt", "class FooTest\n")
            result = run_guard(CI_CHECK_ROBOLECTRIC, Path(tmp))
        self.assertEqual(result.returncode, 0, result.stdout)


class AutonomyDirsGuardTest(unittest.TestCase):
    """``ci_check_autonomy_dirs.py`` must reject stray ``autonomy/`` dirs."""

    def test_fails_when_canonical_directory_is_missing(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            result = run_guard(CI_CHECK_AUTONOMY, Path(tmp))
        self.assertNotEqual(result.returncode, 0, result.stdout)
        self.assertIn(".autonomy", result.stdout)

    def test_fails_when_a_stray_autonomy_directory_exists(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            (Path(tmp) / ".autonomy").mkdir()
            (Path(tmp) / "autonomy").mkdir()
            result = run_guard(CI_CHECK_AUTONOMY, Path(tmp))
        self.assertNotEqual(result.returncode, 0, result.stdout)
        self.assertIn("stray", result.stdout)

    def test_passes_when_only_canonical_directory_exists(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            (Path(tmp) / ".autonomy").mkdir()
            result = run_guard(CI_CHECK_AUTONOMY, Path(tmp))
        self.assertEqual(result.returncode, 0, result.stdout)


class KotlinStyleGuardTest(unittest.TestCase):
    """``scripts/check_kotlin_style.py`` must reject the ktlint traps."""

    def _check(self, tmp: str, content: str) -> subprocess.CompletedProcess:
        bad = write_file(Path(tmp), "Bad.kt", content)
        return run_guard(CHECK_KOTLIN_STYLE, Path(tmp), str(bad))

    def test_fails_when_file_has_no_final_newline(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            result = self._check(tmp, "class Foo")
        self.assertNotEqual(result.returncode, 0, result.stdout)
        self.assertIn("final-newline", result.stdout)

    def test_fails_when_line_has_trailing_whitespace(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            result = self._check(tmp, "class Foo   \n")
        self.assertNotEqual(result.returncode, 0, result.stdout)
        self.assertIn("trailing", result.stdout.lower())

    def test_fails_when_line_is_tab_indented(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            result = self._check(tmp, "class Foo {\n\tval x = 1\n}\n")
        self.assertNotEqual(result.returncode, 0, result.stdout)
        self.assertIn("indent", result.stdout.lower())

    def test_fails_when_multiline_expression_starts_on_same_line(self) -> None:
        """A multiline expression must start on a new line (ktlint standard:multiline-expression-wrapping)."""
        with tempfile.TemporaryDirectory() as tmp:
            result = self._check(tmp, "val urls = listOf(\n    \"https://example.com/a\",\n    \"https://example.com/b\",\n)\n")
        self.assertNotEqual(result.returncode, 0, result.stdout)
        self.assertIn("multiline-expression", result.stdout.lower())

    def test_passes_when_multiline_expression_starts_on_new_line(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            result = self._check(tmp, "val urls =\n    listOf(\n        \"a\",\n        \"b\",\n    )\n")
            self.assertEqual(result.returncode, 0, result.stdout)

    def test_passes_when_file_is_clean(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            result = self._check(tmp, "class Foo {\n    val x = 1\n}\n")
        self.assertEqual(result.returncode, 0, result.stdout)


if __name__ == "__main__":
    unittest.main(verbosity=2)
