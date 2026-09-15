"""Map git-dirty paths to a feature-gate stack subset."""
from __future__ import annotations

import json
import shlex
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent.parent
STACKS = ("android", "blender", "go", "lightroom", "node", "python", "rust", "web")
HINTS = tuple((f"examples/{s}/", (s,)) for s in STACKS) + (
    ("design-tokens/", ("web", "android")),
    ("branding/", ("web", "android")),
)
WIDE_PREFIX = (
    "scripts/", "tests/", "schemas/", "modules/", ".github/",
    ".cursor/hooks", ".cursor/rules", ".cursor/agents",
    ".cursor-plugin/", ".cursor/plugin",
)
WIDE_NAMES = frozenset(
    "AGENTS.md CLAUDE.md GEMINI.md CONVENTIONS.md .clinerules "
    "bootstrap.config.json bootstrap.config.json.example TEMPLATE_INDEX.json "
    ".gitignore .cursor/stack-selection.json .cursor/hooks.json "
    ".cursor/permissions.json .cursor/worktrees.json".split()
)
DOCS_PREFIX = ("docs/", ".cursor/commands/", ".cursor/skills/")
DOCS_NAMES = frozenset(
    "BUILD_PLAN.md CHANGELOG.md AGENT_MEMORY.md DECISION_LOG.md "
    "COMPLETED_TASKS.md HUMAN_BACKLOG.md README.md SUPPORT.md CITATION.cff "
    "PROMPT_LIBRARY.md KNOWLEDGE_BASE.md LICENSE CODE_OF_CONDUCT.md "
    "SECURITY.md CODEOWNERS AGENT.md AGENT.md.example".split()
)
EPHEMERAL = (
    "/.gradle/", "/node_modules/", "/__pycache__/", "/.pytest_cache/",
    "/.venv/", "/coverage/", "/app/build/", "/.cursor/agent-progress",
    "/.cursor/last-feature-gate", "/.cursor-session-state",
)


def is_ephemeral(path: str) -> bool:
    return any(s in "/" + path.replace("\\", "/").lower() for s in EPHEMERAL)


def changed_paths(root: Path) -> list[str] | None:
    seen: set[str] = set()
    out: list[str] = []
    for args in (
        ("diff", "--name-only", "HEAD"),
        ("diff", "--name-only", "--cached"),
        ("ls-files", "--others", "--exclude-standard"),
    ):
        try:
            proc = subprocess.run(["git", *args], cwd=root, capture_output=True, text=True, check=False)
        except OSError:
            return None
        if proc.returncode != 0:
            return None
        for line in proc.stdout.splitlines():
            p = line.strip().replace("\\", "/")
            if p and p not in seen and not is_ephemeral(p):
                seen.add(p)
                out.append(p)
    return out


def classify(paths: list[str] | tuple[str, ...]) -> dict[str, object]:
    stacks: set[str] = set()
    wide = False
    any_path = False
    for raw in paths:
        p = raw.replace("\\", "/").removeprefix("./")
        if not p or is_ephemeral(p):
            continue
        any_path = True
        hit = next((n for pre, n in HINTS if p.startswith(pre)), None)
        if hit:
            stacks.update(hit)
            continue
        if p in WIDE_NAMES or any(p.startswith(w) for w in WIDE_PREFIX):
            wide = True
            continue
        if p in DOCS_NAMES or any(p.startswith(d) for d in DOCS_PREFIX) or (
            p.endswith(".md") and "/" not in p
        ):
            continue
        wide = True
    if not any_path:
        return {"mode": "docs", "stacks": [], "reason": "no-git-changes"}
    if wide:
        return {"mode": "full", "stacks": list(STACKS), "reason": "shared-or-scripts"}
    if stacks:
        ordered = [s for s in STACKS if s in stacks]
        return {"mode": "stacks", "stacks": ordered, "reason": ",".join(ordered)}
    return {"mode": "docs", "stacks": [], "reason": "docs-only"}


def classify_repo(root: Path | None = None) -> dict[str, object]:
    paths = changed_paths(root or ROOT)
    if paths is None:
        return {"mode": "full", "stacks": list(STACKS), "reason": "git-unavailable"}
    return classify(paths)


def retry_stack(failed_stage: str) -> str | None:
    stage = (failed_stage or "").strip()
    return next((s for s in STACKS if stage.startswith(f"{s}-")), None)


def main(argv: list[str] | None = None) -> int:
    args = list(argv if argv is not None else sys.argv[1:])
    fmt = "json"
    paths: list[str] | None = None
    i = 0
    while i < len(args):
        a = args[i]
        if a == "--shell":
            fmt = "shell"
        elif a == "--paths":
            paths = [p for p in args[i + 1].split(",") if p]
            i += 1
        elif a.startswith("--paths="):
            paths = [p for p in a.split("=", 1)[1].split(",") if p]
        i += 1
    result = classify(paths) if paths is not None else classify_repo()
    if fmt == "shell":
        stacks = ",".join(str(s) for s in result["stacks"])
        print(f"GATE_MODE={shlex.quote(str(result['mode']))}")
        print(f"GATE_STACKS={shlex.quote(stacks)}")
        print(f"GATE_REASON={shlex.quote(str(result['reason']))}")
    else:
        json.dump(result, sys.stdout)
        print()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
