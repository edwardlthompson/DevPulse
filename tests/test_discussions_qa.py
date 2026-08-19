"""Discussions Q&A category name matching."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from discussions_qa import has_qa  # noqa: E402


class DiscussionsQaTests(unittest.TestCase):
    def test_detects_qa_names(self) -> None:
        self.assertTrue(has_qa([{"name": "Q&A", "isAnswerable": True}]))
        self.assertTrue(has_qa([{"name": "qa"}]))
        self.assertFalse(has_qa([{"name": "Ideas"}]))
        self.assertFalse(has_qa([]))
