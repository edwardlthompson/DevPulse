"""Tests for Conventional Commits checker."""
from __future__ import annotations

import sys
from pathlib import Path
import unittest

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from check_conventional_commit import is_conventional  # noqa: E402


class ConventionalCommitTests(unittest.TestCase):
    def test_feat_ok(self) -> None:
        self.assertTrue(is_conventional("feat: add env schema"))

    def test_scoped_ok(self) -> None:
        self.assertTrue(is_conventional("fix(web): handle empty env"))

    def test_merge_ok(self) -> None:
        self.assertTrue(is_conventional("Merge branch 'main' into feat/x"))

    def test_rejects_freeform(self) -> None:
        self.assertFalse(is_conventional("updated stuff"))

    def test_empty_rejected(self) -> None:
        self.assertFalse(is_conventional(""))


if __name__ == "__main__":
    unittest.main()
