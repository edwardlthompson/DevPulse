"""Adapter write + drift checks."""
from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from adapter_templates import ADAPTERS, ADAPTER_MAX_BYTES, GENERATED, POINTER_KEYS, POINTER_MAX_LINES  # noqa: E402
from agent_adapters import check_adapters, expected_text, write_adapters  # noqa: E402


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
            self.assertIn("AGENTS.md", names)
            self.assertTrue(any(p.as_posix().endswith(".clinerules/AGENTS.md") for p in written))
            self.assertIn("CONVENTIONS.md", names)
            self.assertIn("agents.md", names)
            self.assertEqual(len(written), len(ADAPTERS))

    def test_clinerules_file_migrates_and_preserves_workflows(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            legacy = root / ".clinerules"
            legacy.write_text("legacy pointer\n", encoding="utf-8")
            # After unlink, create workflows beside AGENTS.md via a second write cycle
            # First ensure we can migrate file -> dir
            written = write_adapters(root)
            self.assertFalse(legacy.is_file())
            self.assertTrue((root / ".clinerules").is_dir())
            agents = root / ".clinerules" / "AGENTS.md"
            self.assertTrue(agents.is_file())
            self.assertTrue(any(p == agents for p in written))
            tour = root / ".clinerules" / "workflows" / "tour.md"
            tour.parent.mkdir(parents=True, exist_ok=True)
            tour.write_text("# keep me\nFollow `AGENTS.md`.\n", encoding="utf-8")
            write_adapters(root)
            self.assertEqual(tour.read_text(encoding="utf-8"), "# keep me\nFollow `AGENTS.md`.\n")
            self.assertIn("XML", agents.read_text(encoding="utf-8"))

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

    def test_copilot_cline_byte_budget(self) -> None:
        for key, _rel, body in ADAPTERS:
            cap = ADAPTER_MAX_BYTES.get(key)
            if cap is None:
                continue
            n = len(expected_text(body).encode("utf-8"))
            self.assertLessEqual(n, cap, f"{key} {n} > {cap}")


if __name__ == "__main__":
    unittest.main()
