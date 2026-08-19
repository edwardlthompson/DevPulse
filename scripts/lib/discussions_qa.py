"""Best-effort Discussions Q&A category (GraphQL list, REST/GraphQL create)."""
from __future__ import annotations

import json
import subprocess
import sys

LIST_Q = """
query($owner: String!, $name: String!) {
  repository(owner: $owner, name: $name) {
    id
    discussionCategories(first: 25) {
      nodes { name isAnswerable }
    }
  }
}
"""

CREATE_M = """
mutation($repoId: ID!, $name: String!, $emoji: String!) {
  createDiscussionCategory(input: {
    repositoryId: $repoId
    name: $name
    emoji: $emoji
    isAnswerable: true
  }) {
    category { name }
  }
}
"""

HUMAN = (
    "SKIP Q&A category ([HUMAN] Settings → General → Features → Discussions "
    "→ add Q&A with answers enabled)"
)


def has_qa(nodes: list[dict]) -> bool:
    for node in nodes:
        name = (node.get("name") or "").strip().lower()
        if name in {"q&a", "qa", "q and a"}:
            return True
    return False


def run_gh(args: list[str]) -> tuple[int, str]:
    proc = subprocess.run(["gh", *args], capture_output=True, text=True, check=False)
    return proc.returncode, (proc.stdout or "").strip()


def ensure_qa(repo: str) -> str:
    if "/" not in repo:
        return HUMAN
    owner, name = repo.split("/", 1)
    rc, raw = run_gh(
        [
            "api",
            "graphql",
            "-f",
            f"query={LIST_Q}",
            "-F",
            f"owner={owner}",
            "-F",
            f"name={name}",
        ]
    )
    repo_id = ""
    nodes: list[dict] = []
    if rc == 0:
        try:
            repo_obj = json.loads(raw).get("data", {}).get("repository") or {}
            repo_id = repo_obj.get("id") or ""
            nodes = (repo_obj.get("discussionCategories") or {}).get("nodes") or []
        except json.JSONDecodeError:
            nodes = []
    if has_qa(nodes):
        return "OK   Discussions Q&A category present"
    rc, _ = run_gh(
        [
            "api",
            "--method",
            "POST",
            f"repos/{repo}/discussions/categories",
            "-f",
            "name=Q&A",
            "-f",
            "emoji=:question:",
            "-F",
            "is_answerable=true",
        ]
    )
    if rc == 0:
        return "OK   Discussions Q&A category created"
    if repo_id:
        rc, _ = run_gh(
            [
                "api",
                "graphql",
                "-f",
                f"query={CREATE_M}",
                "-F",
                f"repoId={repo_id}",
                "-F",
                "name=Q&A",
                "-F",
                "emoji=:question:",
            ]
        )
        if rc == 0:
            return "OK   Discussions Q&A category created"
    return HUMAN


def main(argv: list[str] | None = None) -> int:
    args = argv if argv is not None else sys.argv[1:]
    repo = args[0] if args else ""
    print(ensure_qa(repo))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
