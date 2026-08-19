"""Plain-English gate hint mapping."""
from __future__ import annotations

import json
import unittest
from io import StringIO
from unittest.mock import patch

from gate_hints import format_human, hint_for, main


class GateHintTests(unittest.TestCase):
    def test_known_stage(self) -> None:
        hint = hint_for("python-lint")
        self.assertIn("ruff", hint["run"])
        self.assertTrue(hint["suggested"])
        text = format_human("python-lint", "ruff failed")
        self.assertIn("What failed: python-lint", text)
        self.assertIn("What that means:", text)
        self.assertIn("What to run:", text)
        self.assertIn("Why:", text)
        self.assertIn("Log: ruff failed", text)

    def test_unknown_stage_fallback(self) -> None:
        hint = hint_for("not-a-real-stage")
        self.assertIn("feature-autofix", hint["run"])
        text = format_human("", "")
        self.assertIn("What failed: unknown", text)

    def test_cli_json(self) -> None:
        buf = StringIO()
        with patch("sys.stdout", buf):
            self.assertEqual(main(["--json", "environment", "cargo not found"]), 0)
        data = json.loads(buf.getvalue())
        self.assertIn("human_hint", data)
        self.assertIn("toolchain", data["means"])

    def test_verify_stages(self) -> None:
        env = hint_for("verify-env")
        self.assertIn("check-env", env["run"])
        boot = hint_for("verify-bootstrap")
        self.assertIn("validate-bootstrap", boot["run"])
        feat = hint_for("verify-feature-gate")
        self.assertIn("feature-gate", feat["run"])
        text = format_human("verify-env")
        self.assertIn("What failed: verify-env", text)


if __name__ == "__main__":
    unittest.main()
