"""CHANGELOG Unreleased order and emptiness."""
from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from changelog_unreleased import check, emptied, fold  # noqa: E402


class ChangelogUnreleasedTests(unittest.TestCase):
    def test_first_and_empty(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "CHANGELOG.md"
            path.write_text(
                "# Changelog\n\n## [Unreleased]\n\n## [0.1.0]\n",
                encoding="utf-8",
            )
            self.assertEqual(check(path, require_empty=True), [])

    def test_wrong_order_and_content(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "CHANGELOG.md"
            path.write_text(
                "# Changelog\n\n## [0.1.0]\n\n## [Unreleased]\n\n### Added\n\n* x\n",
                encoding="utf-8",
            )
            errors = check(path, require_empty=True)
            self.assertTrue(any("first" in e for e in errors))
            self.assertTrue(any("empty" in e for e in errors))

    def test_fold_extracts_and_empties(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "CHANGELOG.md"
            path.write_text(
                "# Changelog\n\n## [Unreleased]\n\n### Added\n\n* ship fix\n\n"
                "## [0.1.0]\n\n* old\n",
                encoding="utf-8",
            )
            notes = fold(path)
            self.assertIn("* ship fix", notes)
            self.assertNotIn("* old", notes)
            text = path.read_text(encoding="utf-8")
            self.assertIn("## [Unreleased]\n\n## [0.1.0]", text)
            self.assertEqual(check(path, require_empty=True), [])
            dirty = "# Changelog\n\n## [Unreleased]\n\n* leftover\n\n## [0.1.0]\n"
            self.assertIn("## [Unreleased]\n\n## [0.1.0]", emptied(dirty))
