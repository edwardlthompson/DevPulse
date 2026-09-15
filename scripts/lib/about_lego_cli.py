"""Backup, strip, and restore About slices for rust/go/node/python Golden Paths."""

from __future__ import annotations

import argparse
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
STUBS = ROOT / "schemas" / "golden-path" / "about-without"
TRACKED = (
    "examples/rust/src/lib.rs",
    "examples/rust/src/main.rs",
    "examples/rust/src/about.rs",
    "examples/rust/src/log.rs",
    "examples/go/main.go",
    "examples/go/log.go",
    "examples/go/about.go",
    "examples/go/about_test.go",
    "examples/go/http_about.go",
    "examples/go/http_about_test.go",
    "examples/node/src/app.ts",
    "examples/node/src/about.ts",
    "examples/node/src/about.test.ts",
    "examples/node/src/app.test.ts",
    "examples/node/src/openapi.test.ts",
    "examples/python/src/hello/cli.py",
    "examples/python/src/hello/about.py",
    "examples/python/tests/test_about.py",
    "examples/python/tests/test_about_parity.py",
    "examples/python/tests/test_cli.py",
    "examples/python/tests/test_openapi.py",
)


def write_lf(path: Path, text: str) -> None:
    path.write_bytes(text.replace("\r\n", "\n").encode("utf-8"))


def _copy_stub(name: str, dest: Path) -> None:
    write_lf(dest, (STUBS / name).read_text(encoding="utf-8"))


def backup(dest: Path, root: Path) -> None:
    for rel in TRACKED:
        src = root / rel
        if src.is_file():
            out = dest / rel
            out.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(src, out)


def restore(src: Path, root: Path) -> None:
    copied = False
    for rel in TRACKED:
        bak = src / rel
        if bak.is_file():
            dest = root / rel
            dest.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(bak, dest)
            copied = True
    if not copied:
        raise SystemExit("about CLI backup missing")


def _drop(text: str, pattern: str) -> str:
    patched, n = re.subn(pattern, "\n", text, count=1, flags=re.S)
    if n != 1:
        raise SystemExit(f"could not strip About test: {pattern[:48]}")
    return patched


def _drop_all(text: str, pattern: str) -> str:
    while True:
        patched, n = re.subn(pattern, "\n", text, count=1, flags=re.S)
        if n == 0:
            return text
        text = patched


def _unlink(root: Path, *rels: str) -> None:
    for rel in rels:
        (root / rel).unlink(missing_ok=True)


def _cut(path: Path, needle: str) -> None:
    if path.is_file():
        write_lf(path, path.read_text(encoding="utf-8").replace(needle, ""))


def strip(root: Path) -> None:
    _unlink(
        root,
        "examples/rust/src/about.rs",
        "examples/go/about.go",
        "examples/go/http_about.go",
        "examples/go/http_about_test.go",
    )
    lib = (root / "examples/rust/src/lib.rs").read_text(encoding="utf-8")
    write_lf(root / "examples/rust/src/lib.rs", lib.replace("pub mod about;\n", ""))
    _copy_stub("rust-main.rs", root / "examples/rust/src/main.rs")
    _cut(root / "examples/rust/src/log.rs", '    let _ = writeln!(stdout, "{}", crate::about::summary());\n')
    _copy_stub("go-main.go", root / "examples/go/main.go")
    _cut(root / "examples/go/log.go", "\tfmt.Fprintln(stdout, AboutSummary())\n")
    go_test = root / "examples/go/about_test.go"
    write_lf(go_test, _drop_all(go_test.read_text(encoding="utf-8"), r"\nfunc TestAbout[A-Za-z0-9]*\([\s\S]*?\n\}\n"))
    _unlink(root, "examples/node/src/about.ts", "examples/node/src/about.test.ts", "examples/node/src/openapi.test.ts")
    _copy_stub("node-app.ts", root / "examples/node/src/app.ts")
    node_test = root / "examples/node/src/app.test.ts"
    text = _drop_all(
        node_test.read_text(encoding="utf-8"),
        r"\n  it\(\"(?:returns About payload|returns a GitHub feedback URL)[\s\S]*?\n  \}\);\n",
    )
    write_lf(node_test, re.sub(r"\n+\n\}\);\s*\Z", "\n});\n", text))
    _unlink(root, "examples/python/src/hello/about.py", "examples/python/tests/test_about.py", "examples/python/tests/test_about_parity.py")
    _copy_stub("python-cli.py", root / "examples/python/src/hello/cli.py")
    _copy_stub("python-test-openapi.py", root / "examples/python/tests/test_openapi.py")
    py_test = root / "examples/python/tests/test_cli.py"
    text = _drop(py_test.read_text(encoding="utf-8"), r"\n\ndef test_main_about[\s\S]*\Z")
    write_lf(py_test, text.replace("import json\n", "").rstrip() + "\n")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("action", choices=("backup", "strip", "restore"))
    parser.add_argument("backup_dir", nargs="?", default="")
    parser.add_argument("--root", default=str(ROOT))
    args = parser.parse_args()
    root = Path(args.root)
    dest = Path(args.backup_dir) if args.backup_dir else Path()
    if args.action == "backup":
        backup(dest, root)
    elif args.action == "strip":
        strip(root)
    else:
        restore(dest, root)


if __name__ == "__main__":
    main()
