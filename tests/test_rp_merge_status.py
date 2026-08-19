"""Release Please wait-skip helper."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from rp_merge_status import skip_auto_merge_wait  # noqa: E402


class RpMergeStatusTests(unittest.TestCase):
    def test_empty_or_action_required(self) -> None:
        self.assertTrue(skip_auto_merge_wait([]))
        self.assertTrue(
            skip_auto_merge_wait([{"conclusion": "ACTION_REQUIRED"}])
        )
        self.assertFalse(
            skip_auto_merge_wait([{"conclusion": "SUCCESS"}], "BLOCKED")
        )
        self.assertFalse(skip_auto_merge_wait([], "MERGED"))
