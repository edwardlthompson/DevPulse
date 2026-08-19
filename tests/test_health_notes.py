"""Working-tree notes for project-health."""
from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from health_notes import collect_health_notes, unreleased_has_entries  # noqa: E402


class HealthNoteTests(unittest.TestCase):
    def test_unreleased_entries(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "CHANGELOG.md").write_text(
                "## [Unreleased]\n\n### Added\n\n* **x:** y\n\n## [0.1.0]\n",
                encoding="utf-8",
            )
            self.assertTrue(unreleased_has_entries(root))
            (root / "CHANGELOG.md").write_text(
                "## [Unreleased]\n\n### Added\n\n## [0.1.0]\n",
                encoding="utf-8",
            )
            self.assertFalse(unreleased_has_entries(root))

    def test_collect_notes(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "CHANGELOG.md").write_text(
                "## [Unreleased]\n\n* item\n",
                encoding="utf-8",
            )
            with patch("health_notes._git", return_value=" M README.md\n"):
                notes = collect_health_notes(root)
            self.assertTrue(any("dirty" in n.lower() for n in notes))
            self.assertTrue(any("Unreleased" in n for n in notes))
