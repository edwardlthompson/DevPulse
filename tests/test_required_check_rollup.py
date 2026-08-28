"""Required-check rollup helpers and workflow job names."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LIB = ROOT / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from required_check_rollup import (  # noqa: E402
    REQUIRED_CONTEXTS,
    context_ok,
    jobs_ok,
    missing_required,
    parse_job_pairs,
    rollup_ready,
)


class JobResultTests(unittest.TestCase):
    def test_success_and_skipped_pass(self) -> None:
        self.assertTrue(
            jobs_ok({"Feature Gate": "success", "web": "SKIPPED"})
        )

    def test_empty_or_failure_fails(self) -> None:
        self.assertFalse(jobs_ok({}))
        self.assertFalse(jobs_ok({"CI": "failure"}))
        self.assertFalse(jobs_ok({"CI": "cancelled"}))

    def test_parse_pairs_and_comments(self) -> None:
        pairs = parse_job_pairs(
            ["Feature Gate=success", "# ignore", "  web=skipped", ""]
        )
        self.assertEqual(pairs, {"Feature Gate": "success", "web": "skipped"})
        with self.assertRaises(ValueError):
            parse_job_pairs(["nocolon"])
        with self.assertRaises(ValueError):
            parse_job_pairs(["=success"])


class ContextTests(unittest.TestCase):
    def test_neutral_codeql_does_not_count(self) -> None:
        runs = [
            {
                "name": "CodeQL",
                "conclusion": "NEUTRAL",
                "app": {"slug": "github-advanced-security"},
            }
        ]
        self.assertFalse(context_ok(runs, "CodeQL"))
        self.assertIn("CodeQL", missing_required(runs))

    def test_actions_success_beats_ghas_neutral(self) -> None:
        runs = [
            {
                "name": "CodeQL",
                "conclusion": "neutral",
                "app": {"slug": "github-advanced-security"},
            },
            {
                "name": "CodeQL",
                "conclusion": "success",
                "app": {"slug": "github-actions"},
            },
        ]
        self.assertTrue(context_ok(runs, "CodeQL"))
        self.assertTrue(rollup_ready(runs + [
            {"name": n, "conclusion": "success"}
            for n in REQUIRED_CONTEXTS
            if n != "CodeQL"
        ]))

    def test_commit_status_satisfies_missing_job(self) -> None:
        self.assertTrue(
            context_ok([], "CI", statuses=[{"context": "CI", "state": "success"}])
        )
        self.assertFalse(
            context_ok([], "CI", statuses=[{"context": "CI", "state": "pending"}])
        )


class WorkflowJobNameTests(unittest.TestCase):
    def test_workflows_publish_required_job_names(self) -> None:
        ci = (ROOT / ".github/workflows/ci.yml").read_text(encoding="utf-8")
        sec = (ROOT / ".github/workflows/security.yml").read_text(encoding="utf-8")
        ql = (ROOT / ".github/workflows/codeql.yml").read_text(encoding="utf-8")
        self.assertIn("\n    name: CI\n", ci)
        self.assertIn("conclude-required-check.sh", ci)
        self.assertIn("\n    name: Security Scan\n", sec)
        self.assertIn("conclude-required-check.sh", sec)
        self.assertIn("\n    name: CodeQL\n", ql)
        self.assertIn("conclude-required-check.sh", ql)


if __name__ == "__main__":
    unittest.main()
