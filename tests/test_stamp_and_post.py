"""Tests for AGENTS.md stamp and optional post hooks."""
from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from bootstrap_cli import run  # noqa: E402
from bootstrap_engine import default_config, save_config  # noqa: E402
from bootstrap_post import create_welcome_issue, ensure_git_repo, install_deps  # noqa: E402
from build_sprint_model import is_template_repo  # noqa: E402
from stamp_project import stamp_agents_md  # noqa: E402

REPO = Path(__file__).resolve().parent.parent


class StampTests(unittest.TestCase):
    def test_inserts_and_updates_card(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "AGENTS.md").write_text(
                "# Agent Router\n\n## Project Overview & Architecture\n\nHello\n",
                encoding="utf-8",
            )
            stamp_agents_md(root, name="Demo", purpose="Notes", stack="web")
            text = (root / "AGENTS.md").read_text(encoding="utf-8")
            self.assertIn("**Product:** Demo", text)
            stamp_agents_md(root, name="Demo2", purpose="Notes", stack="python")
            text = (root / "AGENTS.md").read_text(encoding="utf-8")
            self.assertIn("**Product:** Demo2", text)
            self.assertNotIn("**Product:** Demo\n", text)
            self.assertEqual(text.count("<!-- bootstrap-project-card -->"), 1)

    def test_template_purpose_is_portable(self) -> None:
        agents = (REPO / "AGENTS.md").read_text(encoding="utf-8")
        cfg = (REPO / "bootstrap.config.json").read_text(encoding="utf-8")
        self.assertNotIn("FOSS Cursor agent projects", agents)
        self.assertNotIn("FOSS Cursor agent projects", cfg)
        if is_template_repo(REPO):
            self.assertIn("coding-agent", agents)


class PostHookTests(unittest.TestCase):
    def test_git_already_present(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / ".git").mkdir()
            self.assertIn("already present", ensure_git_repo(root))

    def test_git_init_fails_without_git(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            with patch("bootstrap_post.tool_present", return_value=False):
                with self.assertRaises(RuntimeError) as ctx:
                    ensure_git_repo(Path(tmp))
            self.assertIn("git is required", str(ctx.exception))

    def test_install_deps_noop(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            notes = install_deps(Path(tmp), "web")
            self.assertTrue(any("no stack" in n for n in notes))

    def test_welcome_issue_requires_gh(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            with patch("bootstrap_post.tool_present", return_value=False):
                with self.assertRaises(RuntimeError) as ctx:
                    create_welcome_issue(Path(tmp))
            self.assertIn("gh is required", str(ctx.exception))

    def test_welcome_issue_creates(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            fake = type("P", (), {"returncode": 0, "stdout": "https://example/1\n", "stderr": ""})()
            with patch("bootstrap_post.tool_present", return_value=True):
                with patch("bootstrap_post.subprocess.run", return_value=fake):
                    note = create_welcome_issue(Path(tmp))
            self.assertIn("https://example/1", note)

    def test_post_merges_welcome_hook(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "AGENTS.md").write_text("# Agent\n", encoding="utf-8")
            cfg = default_config(project_name="T", purpose="P", stack="none")
            cfg["hooks"]["post_welcome_issue"] = True
            save_config(root, cfg)
            with patch(
                "bootstrap_cli.create_welcome_issue", return_value="opened welcome issue"
            ) as welcome:
                self.assertEqual(run(["--root", str(root), "--post", "--skip-preflight"]), 0)
                welcome.assert_called_once()
            saved = (root / "bootstrap.config.json").read_text(encoding="utf-8")
            self.assertIn('"post_welcome_issue": true', saved)


if __name__ == "__main__":
    unittest.main()
