"""Generate thin multi-agent adapters from AGENTS.md (source of truth)."""
from __future__ import annotations

import json
import sys
from pathlib import Path

from adapter_templates import ADAPTERS, GENERATED, POINTER_KEYS, POINTER_MAX_LINES


def write_adapters(root: Path, enabled: dict[str, bool] | None = None) -> list[Path]:
    flags = enabled or {}
    written: list[Path] = []
    for key, rel, body in ADAPTERS:
        if flags.get(key, True) is False:
            continue
        path = root / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(body.lstrip() + "\n", encoding="utf-8")
        written.append(path)
    return written


def expected_text(body: str) -> str:
    return body.lstrip() + "\n"


def line_count(text: str) -> int:
    if not text:
        return 0
    return text.count("\n") + (0 if text.endswith("\n") else 1)


def check_adapters(root: Path, enabled: dict[str, bool] | None = None) -> list[str]:
    flags = enabled or {}
    errors: list[str] = []
    for key, rel, body in ADAPTERS:
        if flags.get(key, True) is False:
            continue
        path = root / rel
        want = expected_text(body)
        if not path.is_file():
            errors.append(f"MISSING: {rel.as_posix()}")
            continue
        text = path.read_text(encoding="utf-8")
        if GENERATED not in text:
            errors.append(f"MISSING_HEADER: {rel.as_posix()}")
        if text != want:
            errors.append(
                f"DRIFT: {rel.as_posix()} (run bash scripts/bootstrap-lifecycle.sh --sync-adapters)"
            )
        if key in POINTER_KEYS and line_count(text) > POINTER_MAX_LINES:
            errors.append(
                f"POINTER_TOO_LONG: {rel.as_posix()} "
                f"({line_count(text)} lines, max {POINTER_MAX_LINES})"
            )
    return errors


def flags_from_root(root: Path) -> dict[str, bool]:
    path = root / "bootstrap.config.json"
    if not path.is_file():
        path = root / "bootstrap.config.json.example"
    if not path.is_file():
        return {}
    data = json.loads(path.read_text(encoding="utf-8"))
    adapters = data.get("agent_adapters") if isinstance(data, dict) else None
    if not isinstance(adapters, dict):
        return {}
    return {str(k): bool(v) for k, v in adapters.items()}


def main(argv: list[str] | None = None) -> int:
    args = argv if argv is not None else sys.argv[1:]
    root = Path.cwd()
    flags = flags_from_root(root)
    if args and args[0] == "--write":
        written = write_adapters(root, flags)
        for path in written:
            print(path.as_posix())
        return 0
    errors = check_adapters(root, flags)
    if errors:
        print("\n".join(errors))
        print("Adapter check failed. Re-sync: bash scripts/bootstrap-lifecycle.sh --sync-adapters")
        return 1
    print("Agent adapters OK")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
