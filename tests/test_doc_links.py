"""Relative-link checker for docs/**."""
from __future__ import annotations

import sys
import tempfile
import unittest
from io import StringIO
from pathlib import Path
from unittest.mock import patch

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from check_doc_links import check_file, collect_errors, main  # noqa: E402


class DocLinkTests(unittest.TestCase):
    def test_ok_and_broken(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            docs = root / "docs"
            docs.mkdir()
            (docs / "ok.md").write_text("[here](other.md)\n", encoding="utf-8")
            (docs / "other.md").write_text("x\n", encoding="utf-8")
            (docs / "bad.md").write_text("[gone](missing.md)\n", encoding="utf-8")
            self.assertEqual(check_file(docs / "ok.md"), [])
            errors = collect_errors(root)
            self.assertTrue(any("missing.md" in e for e in errors))
            buf = StringIO()
            with patch("sys.stdout", buf):
                self.assertEqual(main([str(root)]), 1)
            self.assertIn("missing.md", buf.getvalue())

    def test_root_markdown(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "docs").mkdir()
            (root / "docs" / "ok.md").write_text("x\n", encoding="utf-8")
            (root / "AGENTS.md").write_text("[gone](no-such.md)\n", encoding="utf-8")
            (root / "COMPLETED_TASKS.md").write_text("[skip](also-missing.md)\n", encoding="utf-8")
            errors = collect_errors(root)
            self.assertTrue(any("AGENTS.md" in e and "no-such.md" in e for e in errors))
            self.assertFalse(any("COMPLETED_TASKS.md" in e for e in errors))

    def test_skips_http(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "docs" / "web.md"
            path.parent.mkdir()
            path.write_text("[ext](https://example.com/x)\n", encoding="utf-8")
            self.assertEqual(check_file(path), [])

    def test_pruned_module_link_ok(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            docs = root / "docs"
            docs.mkdir()
            (docs / "note.md").write_text(
                "[android](../modules/android/COMMERCIAL.md)\n",
                encoding="utf-8",
            )
            self.assertEqual(collect_errors(root), [])
            (root / "modules" / "android").mkdir(parents=True)
            errors = collect_errors(root)
            self.assertTrue(any("COMMERCIAL.md" in e for e in errors))
            (root / "modules" / "android" / "COMMERCIAL.md").write_text(
                "x\n", encoding="utf-8"
            )
            self.assertEqual(collect_errors(root), [])
