"""Patch-only Gradle plugin pin apply with Kotlin CodeQL ceiling."""
from __future__ import annotations

import os
import re
import sys
from pathlib import Path

from update_deps import KOTLIN_BLOCK, PLUGIN_RE

HINT = (
    "Set UPDATE_GRADLE_PINS='plugin.id=x.y.z,...' for patch-only Gradle apply "
    "(Kotlin < 2.3.30)."
)


def parse_ver(text: str) -> tuple[int, int, int] | None:
    parts = text.split(".")
    if len(parts) < 2 or not all(p.isdigit() for p in parts[:3]):
        return None
    nums = [int(p) for p in parts[:3]]
    while len(nums) < 3:
        nums.append(0)
    return nums[0], nums[1], nums[2]


def is_patch_bump(old: str, new: str) -> bool:
    left, right = parse_ver(old), parse_ver(new)
    if left is None or right is None:
        return False
    return left[0] == right[0] and left[1] == right[1] and right >= left


def kotlin_ok(plugin: str, ver: str) -> bool:
    if "kotlin" not in plugin.lower():
        return True
    parsed = parse_ver(ver)
    return parsed is not None and parsed < KOTLIN_BLOCK


def parse_pins(raw: str) -> dict[str, str]:
    out: dict[str, str] = {}
    for item in raw.split(","):
        item = item.strip()
        if not item or "=" not in item:
            continue
        name, ver = item.split("=", 1)
        out[name.strip()] = ver.strip()
    return out


def apply_mapping(text: str, mapping: dict[str, str]) -> tuple[str, list[str]]:
    changed: list[str] = []

    def repl(match: re.Match[str]) -> str:
        name, ver = match.group(1), match.group(2)
        nxt = mapping.get(name)
        if not nxt or nxt == ver:
            return match.group(0)
        if not is_patch_bump(ver, nxt):
            changed.append(f"SKIP {name}: {ver} -> {nxt} (not patch)")
            return match.group(0)
        if not kotlin_ok(name, nxt):
            changed.append(f"SKIP {name}: Kotlin {nxt} blocked")
            return match.group(0)
        changed.append(f"{name}: {ver} -> {nxt}")
        return f'id("{name}") version "{nxt}"'

    return PLUGIN_RE.sub(repl, text), changed


def apply_env_pins(root: Path, raw: str, *, write: bool) -> list[str]:
    mapping = parse_pins(raw)
    path = root / "examples" / "android" / "build.gradle.kts"
    if not mapping or not path.is_file():
        return []
    text = path.read_text(encoding="utf-8")
    updated, changed = apply_mapping(text, mapping)
    if write and updated != text:
        path.write_text(updated, encoding="utf-8", newline="\n")
    return changed


def apply_depsonar_pins(
    root: Path, pins: dict[str, str] | str, *, write: bool
) -> list[str]:
    """Apply depsonar Gradle plugin updates through patch-only + Kotlin cap."""
    raw = pins if isinstance(pins, str) else ",".join(f"{k}={v}" for k, v in pins.items())
    return apply_env_pins(root, raw, write=write)


def main(argv: list[str] | None = None) -> int:
    import argparse

    parser = argparse.ArgumentParser(description="Apply depsonar Gradle pins")
    parser.add_argument("--pins", default="", help="id=ver,id=ver")
    parser.add_argument("--apply", action="store_true")
    parsed = parser.parse_args(argv)
    root = Path.cwd()
    raw = parsed.pins or os.environ.get("UPDATE_GRADLE_PINS", "")
    if not raw:
        print("FAIL: pass --pins id=ver or UPDATE_GRADLE_PINS", file=sys.stderr)
        return 1
    for line in apply_depsonar_pins(root, raw, write=parsed.apply):
        prefix = "Gradle pin" if parsed.apply else "Gradle pin (dry-run)"
        print(f"{prefix}: {line}")
    from update_deps import kotlin_guard_error

    err = kotlin_guard_error(root)
    if err:
        print(f"FAIL: {err}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
