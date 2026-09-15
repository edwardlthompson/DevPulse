"""Use Gradle --offline after the first successful worktree warm-up."""
from __future__ import annotations

import sys
from pathlib import Path

STAMP_NAME = ".gradle-offline-ok"


def stamp_path(primary_root: Path) -> Path:
    return primary_root / ".cursor" / "worktrees" / STAMP_NAME


def should_offline(primary_root: Path | None) -> bool:
    if primary_root is None:
        return False
    try:
        return stamp_path(primary_root).is_file()
    except OSError:
        return False


def extra_args(primary_root: Path | None) -> list[str]:
    return ["--offline"] if should_offline(primary_root) else []


def mark_success(primary_root: Path) -> Path:
    path = stamp_path(primary_root)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("ok\n", encoding="utf-8")
    return path


def _root_from_args(args: list[str]) -> Path:
    if "--root" in args:
        return Path(args[args.index("--root") + 1])
    return Path.cwd()


def main(argv: list[str] | None = None) -> int:
    args = list(sys.argv[1:] if argv is None else argv)
    root = _root_from_args(args)
    if "--mark" in args:
        mark_success(root)
        return 0
    print(" ".join(extra_args(root)))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
