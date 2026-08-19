"""Validate .env.example (and optional .env) against env.schema.json."""
from __future__ import annotations

import json
import re
from pathlib import Path

KEY_RE = re.compile(r"^\s*(?:export\s+)?([A-Z][A-Z0-9_]+)\s*=")
COMMENT_KEY_RE = re.compile(r"^\s*#\s*(?:export\s+)?([A-Z][A-Z0-9_]+)\s*=")


def parse_env_keys(text: str, *, include_comments: bool = True) -> set[str]:
    keys: set[str] = set()
    for line in text.splitlines():
        m = KEY_RE.match(line)
        if m:
            keys.add(m.group(1))
            continue
        if include_comments:
            c = COMMENT_KEY_RE.match(line)
            if c:
                keys.add(c.group(1))
    return keys


def load_schema(root: Path) -> list[dict]:
    path = root / "env.schema.json"
    data = json.loads(path.read_text(encoding="utf-8"))
    vars_ = data.get("vars")
    if not isinstance(vars_, list):
        raise ValueError("env.schema.json missing vars array")
    return [v for v in vars_ if isinstance(v, dict) and v.get("name")]


def validate_env(root: Path) -> list[str]:
    errors: list[str] = []
    schema_path = root / "env.schema.json"
    example = root / ".env.example"
    if not schema_path.is_file():
        return ["missing env.schema.json"]
    if not example.is_file():
        return ["missing .env.example"]
    try:
        schema = load_schema(root)
    except (OSError, json.JSONDecodeError, ValueError) as exc:
        return [f"invalid env.schema.json: {exc}"]
    example_keys = parse_env_keys(example.read_text(encoding="utf-8"), include_comments=True)
    for item in schema:
        name = str(item["name"])
        if name not in example_keys:
            errors.append(f".env.example missing schema key: {name}")
    live = root / ".env"
    if live.is_file():
        live_keys = parse_env_keys(live.read_text(encoding="utf-8"), include_comments=False)
        for item in schema:
            if item.get("required") and str(item["name"]) not in live_keys:
                errors.append(f".env missing required key: {item['name']}")
    return errors
