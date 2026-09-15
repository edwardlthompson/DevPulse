"""Lint Golden Path Lightroom Lua: Lr* imports only, no OS shells."""

from __future__ import annotations

import re
import shutil
import subprocess
import sys
from pathlib import Path

FORBIDDEN = re.compile(
    r"""(?x)
    require\s*\(
    | os\.execute
    | io\.popen
    | loadfile\s*\(
    | dofile\s*\(
    | package\.
    | debug\.
    """
)
IMPORT = re.compile(r"""import\s*['"]([^'"]+)['"]""")
LR_MODULE = re.compile(r"^Lr[A-Za-z][A-Za-z0-9]*$")
REQUIRED_FILES = {
    "Info.lua": ("LrSdkVersion", "LrExportServiceProvider", "LrMetadataTagsetFactory"),
    "ExportServiceProvider.lua": ("processRenderedPhotos",),
    "MetadataTagset.lua": ("com.example.fossplugin.tagset",),
}


def _code_lines(text: str) -> list[str]:
    lines: list[str] = []
    for raw in text.splitlines():
        stripped = raw.split("--", 1)[0].strip()
        if stripped:
            lines.append(stripped)
    return lines


def check_lua(path: Path) -> list[str]:
    errors: list[str] = []
    text = path.read_text(encoding="utf-8")
    rel = path.name
    for line in _code_lines(text):
        if FORBIDDEN.search(line):
            errors.append(f"{rel}: forbidden Lua/OS call: {line}")
        for match in IMPORT.finditer(line):
            name = match.group(1)
            if not LR_MODULE.match(name):
                errors.append(f"{rel}: import must be Lr* SDK, got {name}")
    return errors


def check(root: Path) -> list[str]:
    folder = root / "examples" / "lightroom"
    if not folder.is_dir():
        return []
    errors: list[str] = []
    rc = folder / ".luacheckrc"
    if not rc.is_file():
        errors.append("examples/lightroom/.luacheckrc is missing")
    elif "import" not in rc.read_text(encoding="utf-8"):
        errors.append(".luacheckrc must allow the Lightroom import global")
    lua_files = sorted(folder.glob("*.lua"))
    if not lua_files:
        errors.append("examples/lightroom has no .lua files")
    for name, needles in REQUIRED_FILES.items():
        text = (folder / name).read_text(encoding="utf-8") if (folder / name).is_file() else ""
        for needle in needles:
            if needle not in text:
                errors.append(f"{name} must contain {needle}")
    for path in lua_files:
        errors.extend(check_lua(path))
    gate = (root / "scripts/feature-gate.sh").read_text(encoding="utf-8")
    if "check-lightroom-lua.sh" not in gate:
        errors.append("feature-gate.sh must run check-lightroom-lua.sh")
    if shutil.which("luacheck"):
        proc = subprocess.run(
            ["luacheck", str(folder)],
            capture_output=True,
            text=True,
            check=False,
        )
        if proc.returncode != 0:
            errors.append((proc.stdout or proc.stderr or "luacheck failed").strip())
    return errors


def main() -> int:
    errors = check(Path.cwd())
    if errors:
        print("\n".join(errors))
        return 1
    print("Lightroom Lua lint passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
