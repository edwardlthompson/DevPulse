"""Tests for FUNDING.yml and GitHub topics writers."""
from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from init_extras import (  # noqa: E402
    donation_url_usable,
    gh_topics_command,
    merge_topics,
    write_funding_yml,
    write_topics,
)


class FundingTests(unittest.TestCase):
    def test_rejects_placeholder(self) -> None:
        self.assertFalse(donation_url_usable("[INSERT DONATION URL]"))
        self.assertFalse(donation_url_usable(""))
        self.assertTrue(donation_url_usable("https://example.com/donate"))

    def test_writes_only_when_usable(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            self.assertIsNone(write_funding_yml(root, ""))
            self.assertFalse((root / ".github" / "FUNDING.yml").exists())
            path = write_funding_yml(root, "https://example.com/donate")
            assert path is not None
            self.assertIn("https://example.com/donate", path.read_text(encoding="utf-8"))


class TopicsTests(unittest.TestCase):
    def test_merge_and_command(self) -> None:
        text = "# About\n\n## Topics\n\nold\n\n## Child\n\nx\n"
        out = merge_topics(text, ["FOSS", "cursor"])
        self.assertIn("foss, cursor", out)
        self.assertIn("discoverability", out)
        self.assertEqual(gh_topics_command(["FOSS", "cursor"]), "gh repo edit --add-topic foss,cursor")

    def test_empty_topics_noop(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            about = root / "docs" / "GITHUB_ABOUT.md"
            about.parent.mkdir()
            about.write_text("# About\n", encoding="utf-8")
            self.assertIsNone(write_topics(root, []))
            self.assertEqual(about.read_text(encoding="utf-8"), "# About\n")


if __name__ == "__main__":
    unittest.main()
