"""Tests for env.schema.json validation."""
from __future__ import annotations

import json
import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from env_schema import parse_env_keys, validate_env  # noqa: E402


class EnvSchemaTests(unittest.TestCase):
    def test_parses_commented_keys(self) -> None:
        text = "# TEMPLATE_UPSTREAM=owner/repo\nAPI_BASE_URL=http://localhost\n"
        self.assertEqual(parse_env_keys(text), {"TEMPLATE_UPSTREAM", "API_BASE_URL"})

    def test_example_must_list_schema_keys(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "env.schema.json").write_text(
                json.dumps({"vars": [{"name": "API_BASE_URL", "required": False}]}),
                encoding="utf-8",
            )
            (root / ".env.example").write_text("# LOG_LEVEL=info\n", encoding="utf-8")
            errors = validate_env(root)
            self.assertTrue(any("API_BASE_URL" in e for e in errors))

    def test_required_live_env(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "env.schema.json").write_text(
                json.dumps({"vars": [{"name": "API_BASE_URL", "required": True}]}),
                encoding="utf-8",
            )
            (root / ".env.example").write_text("API_BASE_URL=\n", encoding="utf-8")
            (root / ".env").write_text("LOG_LEVEL=info\n", encoding="utf-8")
            errors = validate_env(root)
            self.assertTrue(any("required" in e for e in errors))

    def test_repo_schema_matches_example(self) -> None:
        root = Path(__file__).resolve().parent.parent
        self.assertEqual(validate_env(root), [])


if __name__ == "__main__":
    unittest.main()
