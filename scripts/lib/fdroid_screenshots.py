"""Fail F-Droid listing checks when screenshot files are dummies or incomplete."""
from __future__ import annotations

import struct
from pathlib import Path

DUMMY_NAME = ("dummy", "placeholder", "sample", "lorem")
SCREEN_DIRS = ("phoneScreenshots", "sevenInchScreenshots", "tenInchScreenshots")
ROOTS = (
    Path("examples") / "android" / "metadata",
    Path("examples") / "android" / "fastlane" / "metadata" / "android",
)


def _png_size(path: Path) -> tuple[int, int] | None:
    data = path.read_bytes()
    if len(data) < 24 or data[:8] != b"\x89PNG\r\n\x1a\n":
        return None
    width, height = struct.unpack(">II", data[16:24])
    return width, height


def _is_dummy_name(name: str) -> bool:
    lower = name.lower()
    return any(part in lower for part in DUMMY_NAME)


def _valid_shots(folder: Path) -> list[Path]:
    shots: list[Path] = []
    if not folder.is_dir():
        return shots
    for shot in folder.iterdir():
        if not shot.is_file() or shot.name.startswith("."):
            continue
        if _is_dummy_name(shot.name):
            continue
        size = _png_size(shot)
        if size is None or min(size) <= 8:
            continue
        shots.append(shot)
    return shots


def check_tree(root: Path) -> list[str]:
    errors: list[str] = []
    for base in ROOTS:
        start = root / base
        if not start.is_dir():
            continue
        for folder in start.rglob("*"):
            if not folder.is_dir() or folder.name not in SCREEN_DIRS:
                continue
            for shot in folder.iterdir():
                if not shot.is_file() or shot.name.startswith("."):
                    continue
                rel = shot.relative_to(root).as_posix()
                if _is_dummy_name(shot.name):
                    errors.append(f"dummy screenshot name: {rel}")
                    continue
                size = _png_size(shot)
                if size is None:
                    errors.append(f"screenshot is not a PNG: {rel}")
                elif min(size) <= 8:
                    errors.append(f"dummy screenshot size {size[0]}x{size[1]}: {rel}")
    return errors


def check_completeness(root: Path, *, submit_ready: bool = False) -> list[str]:
    """Scaffold: empty dirs OK. Submit-ready: icon.png + >=1 phoneScreenshots PNG."""
    if not submit_ready:
        return []
    errors: list[str] = []
    meta = root / "examples" / "android" / "metadata" / "en-US" / "images"
    if not meta.is_dir():
        return ["missing examples/android/metadata/en-US/images for submit-ready"]
    icon = meta / "icon.png"
    if not icon.is_file() or _png_size(icon) is None:
        errors.append("submit-ready requires images/icon.png")
    phones = meta / "phoneScreenshots"
    if not _valid_shots(phones):
        errors.append("submit-ready requires >=1 non-dummy phoneScreenshots PNG")
    return errors


def main(argv: list[str] | None = None) -> int:
    import sys

    args = argv if argv is not None else sys.argv[1:]
    submit = "--submit-ready" in args
    root = Path.cwd()
    errors = check_tree(root) + check_completeness(root, submit_ready=submit)
    if errors:
        print("F-Droid screenshot check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    if submit:
        print("F-Droid screenshot submit-ready check passed")
    else:
        print("F-Droid screenshot dummy check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
