"""Adapter write + drift checks."""
from __future__ import annotations

import tempfile
import unittest
from pathlib import Path

from adapter_templates import ADAPTERS, GENERATED, POINTER_KEYS, POINTER_MAX_LINES
from agent_adapters import check_adapters, expected_text, write_adapters


class AdapterWriteTests(unittest.TestCase):
    def test_default_writes_all_targets(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            written = write_adapters(root)
            names = {p.name for p in written}
            self.assertIn("main.mdc", names)
            self.assertIn("CLAUDE.md", names)
            self.assertIn("copilot-instructions.md", names)
            self.assertIn("GEMINI.md", names)
            self.assertIn("agents-pointer.md", names)
            self.assertIn(".clinerules", names)
            self.assertIn("CONVENTIONS.md", names)
            self.assertIn("agents.md", names)
            self.assertEqual(len(written), len(ADAPTERS))

    def test_disable_flag_skips_target(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            written = write_adapters(root, {"gemini": False})
            rels = {p.relative_to(root).as_posix() for p in written}
            self.assertNotIn("GEMINI.md", rels)
            self.assertIn("CLAUDE.md", rels)

    def test_header_and_idempotent(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            write_adapters(root)
            self.assertEqual(check_adapters(root), [])
            write_adapters(root)
            self.assertEqual(check_adapters(root), [])
            for _key, rel, body in ADAPTERS:
                text = (root / rel).read_text(encoding="utf-8")
                self.assertIn(GENERATED, text)
                self.assertEqual(text, expected_text(body))

    def test_drift_and_missing_header(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            write_adapters(root)
            gemini = root / "GEMINI.md"
            gemini.write_text("not a pointer\n", encoding="utf-8")
            errors = check_adapters(root)
            self.assertTrue(any("DRIFT: GEMINI.md" in e for e in errors))
            self.assertTrue(any("MISSING_HEADER: GEMINI.md" in e for e in errors))

    def test_pointer_line_cap(self) -> None:
        for key, _rel, body in ADAPTERS:
            if key not in POINTER_KEYS:
                continue
            lines = expected_text(body).count("\n")
            self.assertLessEqual(lines, POINTER_MAX_LINES, key)


if __name__ == "__main__":
    unittest.main()
