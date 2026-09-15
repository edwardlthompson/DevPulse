"""beforeMCPExecution server allowlist (fail-open if config is missing)."""
from __future__ import annotations

import json
from pathlib import Path

ALLOWLIST = Path(".cursor") / "mcp-allowlist.json"
FOSS_EXAMPLE = Path(".cursor") / "mcp.foss.example"


def load_allowed(root: Path) -> set[str]:
    names: set[str] = set()
    allow = root / ALLOWLIST
    if allow.is_file():
        try:
            data = json.loads(allow.read_text(encoding="utf-8"))
        except json.JSONDecodeError:
            return set()
        names.update(str(item).lower() for item in data.get("servers") or [])
    foss = root / FOSS_EXAMPLE
    if foss.is_file():
        try:
            data = json.loads(foss.read_text(encoding="utf-8"))
        except json.JSONDecodeError:
            data = {}
        names.update(str(key).lower() for key in (data.get("mcpServers") or {}))
    return names


def decide(payload: dict, root: Path) -> dict:
    server = str(payload.get("server") or payload.get("mcp_server") or "").strip()
    if not server:
        return {"permission": "allow"}
    allowed = load_allowed(root)
    if not allowed:
        return {"permission": "allow"}
    if server.lower() in allowed:
        return {"permission": "allow"}
    return {
        "permission": "deny",
        "user_message": f"MCP server '{server}' is not on the FOSS allowlist",
        "agent_message": "Add the server to .cursor/mcp-allowlist.json or use mcp.foss.example names.",
    }


def check_repo(root: Path) -> list[str]:
    errors: list[str] = []
    if not (root / ALLOWLIST).is_file():
        return [f"MISSING: {ALLOWLIST.as_posix()}"]
    allowed = load_allowed(root)
    for name in ("github", "depsonar"):
        if name not in allowed:
            errors.append(f"allowlist must include {name}")
    hook = (root / ".cursor" / "hooks" / "mcp_audit.py").read_text(encoding="utf-8")
    if "decide" not in hook:
        errors.append("mcp_audit.py must enforce the allowlist")
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("MCP allowlist check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("MCP allowlist check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
