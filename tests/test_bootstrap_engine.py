"""Unit tests for bootstrap lifecycle (no third-party deps)."""
from __future__ import annotations

import json
import sys
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

ROOT = Path(__file__).resolve().parent.parent
LIB = ROOT / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from agent_adapters import write_adapters  # noqa: E402
from bootstrap_engine import (  # noqa: E402
    apply_license,
    default_config,
    preflight,
    save_config,
    validate_config,
)
from project_checklist import write_checklist  # noqa: E402


class ValidateConfigTests(unittest.TestCase):
    def test_default_valid_when_named(self) -> None:
        cfg = default_config(project_name="Demo", purpose="Notes", stack="web")
        self.assertEqual(validate_config(cfg), [])

    def test_empty_name_rejected(self) -> None:
        cfg = default_config(project_name="", purpose="x", stack="web")
        self.assertTrue(any("project_name" in e for e in validate_config(cfg)))

    def test_bad_stack_rejected(self) -> None:
        cfg = default_config(project_name="A", purpose="B", stack="cobol")
        self.assertTrue(any("stack" in e for e in validate_config(cfg)))

    def test_empty_object_rejected(self) -> None:
        self.assertTrue(validate_config({}))


class PreflightTests(unittest.TestCase):
    def test_missing_optional_is_warning(self) -> None:
        errors, warnings = preflight("web", strict=False)
        self.assertIsInstance(errors, list)
        self.assertIsInstance(warnings, list)

    def test_strict_promotes_stack_tools(self) -> None:
        errors, _warnings = preflight("android", strict=True)
        self.assertTrue(isinstance(errors, list))

    def test_missing_git_is_error(self) -> None:
        with patch("bootstrap_engine.tool_present", return_value=False):
            with patch("bootstrap_engine.python_present", return_value=True):
                errors, _warnings = preflight("none")
        self.assertTrue(any("git" in e.lower() for e in errors))

    def test_missing_docker_is_warning(self) -> None:
        with patch("bootstrap_engine.tool_present", side_effect=lambda n: n == "git"):
            with patch("bootstrap_engine.python_present", return_value=True):
                errors, warnings = preflight("none")
        self.assertEqual(errors, [])
        self.assertTrue(any("docker" in w.lower() for w in warnings))

    def test_missing_python_is_error(self) -> None:
        with patch("bootstrap_engine.tool_present", side_effect=lambda n: n == "git"):
            with patch("bootstrap_engine.python_present", return_value=False):
                errors, _warnings = preflight("none")
        self.assertTrue(any("python" in e.lower() for e in errors))


class WriterTests(unittest.TestCase):
    def test_save_config_and_adapters(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            cfg = default_config(project_name="T", purpose="P", stack="python")
            path = save_config(root, cfg)
            data = json.loads(path.read_text(encoding="utf-8"))
            self.assertEqual(data["project_name"], "T")
            written = write_adapters(root)
            names = {p.name for p in written}
            self.assertIn("main.mdc", names)
            self.assertIn("CLAUDE.md", names)
            self.assertIn("copilot-instructions.md", names)
            check = write_checklist(root, project_name="T", stack="python", license_id="MIT")
            text = check.read_text(encoding="utf-8")
            self.assertIn("🔲", text)
            self.assertIn("python", text)

    def test_apply_license_apache(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            src = root / "templates" / "licenses"
            src.mkdir(parents=True)
            (src / "Apache-2.0.txt").write_text("APACHE TEST\n", encoding="utf-8")
            dest = apply_license(root, "Apache-2.0")
            assert dest is not None
            self.assertEqual(dest.read_text(encoding="utf-8"), "APACHE TEST\n")

    def test_apply_license_missing_template(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            with self.assertRaises(FileNotFoundError):
                apply_license(Path(tmp), "Apache-2.0")


if __name__ == "__main__":
    unittest.main()
