"""Template vs child next-row for project-health / build-sprint-status."""
from __future__ import annotations

import json
import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from build_sprint import build_status, is_template_repo  # noqa: E402
from weekly_health import is_recurring_weekly_auto  # noqa: E402
from build_sprint_model import PlanRow  # noqa: E402

CHILD_PLAN = """# Build Plan

## Child Repo Playbook (copy after Use this template)

### Sprint 0 — Template Customization

#### Sequential

1. 🔲 [AGENT] Run init-project
"""


class RepoModeTests(unittest.TestCase):
    def test_template_name(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "bootstrap.config.json").write_text(
                json.dumps(
                    {
                        "project_name": "agent-project-bootstrap",
                        "purpose": "x",
                        "stack": "web",
                    }
                ),
                encoding="utf-8",
            )
            self.assertTrue(is_template_repo(root))

    def test_child_product(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "bootstrap.config.json").write_text(
                json.dumps(
                    {
                        "project_name": "notes-app",
                        "purpose": "Offline notes",
                        "stack": "web",
                    }
                ),
                encoding="utf-8",
            )
            self.assertFalse(is_template_repo(root))

    def test_auto_lane_skips_child_playbook_on_template(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "bootstrap.config.json").write_text(
                json.dumps(
                    {
                        "project_name": "agent-project-bootstrap",
                        "purpose": "GitHub Template for FOSS coding-agent projects",
                        "stack": "multi",
                    }
                ),
                encoding="utf-8",
            )
            (root / "BUILD_PLAN.md").write_text(CHILD_PLAN, encoding="utf-8")
            status = build_status(root, lane="auto")
            self.assertEqual(status["lane"], "maintainer")
            task = (status.get("next_row") or {}).get("task", "")
            self.assertNotIn("init-project", task)

    def test_auto_lane_child_still_sees_sprint0(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "bootstrap.config.json").write_text(
                json.dumps(
                    {
                        "project_name": "notes-app",
                        "purpose": "Offline notes",
                        "stack": "web",
                    }
                ),
                encoding="utf-8",
            )
            (root / "BUILD_PLAN.md").write_text(CHILD_PLAN, encoding="utf-8")
            status = build_status(root, lane="auto")
            self.assertEqual(status["lane"], "child")
            self.assertIn("init-project", status["next_row"]["task"])

    def test_weekly_auto_markers(self) -> None:
        row = PlanRow(
            owner="AUTO",
            task="`cursor-feature-radar.sh` (non-blocking; artifact in weekly-health-check)",
            sprint="Ongoing Maintenance",
            phase="maintenance",
        )
        self.assertTrue(is_recurring_weekly_auto(row))
        agent = PlanRow(
            owner="AGENT",
            task="Apply Dependabot bumps; triage Scorecard SARIF findings",
            sprint="Ongoing Maintenance",
            phase="maintenance",
        )
        self.assertFalse(is_recurring_weekly_auto(agent))
