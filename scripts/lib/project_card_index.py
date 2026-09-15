"""Fail when TEMPLATE_INDEX project card drifts from AGENTS.md."""
from __future__ import annotations

import json
import re
from pathlib import Path

START = "<!-- bootstrap-project-card -->"
END = "<!-- /bootstrap-project-card -->"
FIELD = re.compile(r"\*\*(Product|Purpose|Stack):\*\*\s*(.+)")


def parse_agents_card(text: str) -> dict[str, str]:
    if START not in text or END not in text:
        return {}
    body = text.split(START, 1)[1].split(END, 1)[0]
    out: dict[str, str] = {}
    for match in FIELD.finditer(body):
        key = match.group(1).lower()
        if key == "product":
            key = "name"
        out[key] = match.group(2).strip()
    return out


def check_repo(root: Path) -> list[str]:
    errors: list[str] = []
    agents = root / "AGENTS.md"
    index_path = root / "TEMPLATE_INDEX.json"
    if not agents.is_file():
        return ["MISSING: AGENTS.md"]
    if not index_path.is_file():
        return ["MISSING: TEMPLATE_INDEX.json"]
    card = parse_agents_card(agents.read_text(encoding="utf-8"))
    if not card:
        return ["AGENTS.md missing bootstrap-project-card"]
    data = json.loads(index_path.read_text(encoding="utf-8"))
    project = data.get("project")
    if not isinstance(project, dict):
        return ["TEMPLATE_INDEX.json missing project {name,purpose,stack}"]
    for key in ("name", "purpose", "stack"):
        left = str(card.get(key) or "").strip()
        right = str(project.get(key) or "").strip()
        if left != right:
            errors.append(f"project.{key} drift: AGENTS={left!r} INDEX={right!r}")
    cfg_path = root / "bootstrap.config.json"
    if cfg_path.is_file():
        cfg = json.loads(cfg_path.read_text(encoding="utf-8"))
        mapping = {
            "name": "project_name",
            "purpose": "purpose",
            "stack": "stack",
        }
        for idx_key, cfg_key in mapping.items():
            left = str(project.get(idx_key) or "").strip()
            right = str(cfg.get(cfg_key) or "").strip()
            if right and left != right:
                errors.append(f"project.{idx_key} vs bootstrap.config: {left!r} != {right!r}")
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("TEMPLATE_INDEX / AGENTS project card drift:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("TEMPLATE_INDEX project card matches AGENTS.md")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
