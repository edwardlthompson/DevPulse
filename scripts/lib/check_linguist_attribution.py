"""Gate: child .gitattributes must carry Linguist scaffolding attribution."""
from __future__ import annotations

import sys
from collections import Counter
from pathlib import Path

from build_sprint_model import is_template_repo
from linguist_attrs import PATTERNS

SKIP_DIRS = {".git", "node_modules", ".venv", "venv", "dist", "build", "coverage", "__pycache__", ".gradle", "target"}
EXT_LANG = {
    ".py": "Python", ".pyi": "Python", ".sh": "Shell", ".bash": "Shell",
    ".html": "HTML", ".htm": "HTML", ".ps1": "PowerShell", ".psm1": "PowerShell",
    ".kt": "Kotlin", ".kts": "Kotlin", ".java": "Java", ".js": "JavaScript",
    ".jsx": "JavaScript", ".ts": "TypeScript", ".tsx": "TypeScript", ".css": "CSS",
    ".go": "Go", ".rs": "Rust", ".lua": "Lua", ".c": "C", ".cpp": "C++",
}
BAD_DOMINANT = frozenset({"Python", "Shell", "HTML", "PowerShell"})


def _pattern_present(text: str, pattern: str) -> bool:
    for line in text.splitlines():
        stripped = line.strip()
        if stripped == pattern or (stripped.startswith("#") and pattern in stripped):
            return True
    return False


def _prefixes(text: str) -> list[str]:
    out: list[str] = []
    for line in text.splitlines():
        stripped = line.strip()
        if not stripped or stripped.startswith("#"):
            continue
        if "linguist-vendored" in stripped or "linguist-documentation" in stripped:
            out.append(stripped.split()[0].replace("/**", "/").rstrip("*"))
    return out


def _excluded(rel: str, prefixes: list[str]) -> bool:
    posix = rel.replace("\\", "/")
    for prefix in prefixes:
        p = prefix.rstrip("/")
        if posix == p or posix.startswith(p + "/"):
            return True
        if "*/build" in p:
            parts = posix.split("/")
            if len(parts) >= 3 and parts[0] == "examples" and parts[2] == "build":
                return True
    return False


def dominant_language(root: Path, gitattributes: str) -> str | None:
    excluded = _prefixes(gitattributes)
    counts: Counter[str] = Counter()
    for path in root.rglob("*"):
        if not path.is_file() or any(part in SKIP_DIRS for part in path.parts):
            continue
        rel = path.relative_to(root).as_posix()
        if _excluded(rel, excluded):
            continue
        lang = EXT_LANG.get(path.suffix.lower())
        if lang:
            counts[lang] += 1
    return counts.most_common(1)[0][0] if counts else None


def check(root: Path) -> tuple[int, str]:
    if is_template_repo(root):
        return 0, "OK: template repo exempt from linguist attribution gate"
    path = root / ".gitattributes"
    if not path.is_file():
        return 0, "OK: no .gitattributes (linguist attribution optional until init/upgrade)"
    text = path.read_text(encoding="utf-8")
    missing = [p for p in PATTERNS if not _pattern_present(text, p)]
    if missing:
        return 1, (
            "Missing Linguist attribution pattern(s): "
            + ", ".join(missing)
            + ". Fix: copy or append templates/linguist/child.gitattributes "
            "(or re-run init / /upgrade linguist step)."
        )
    lang = dominant_language(root, text)
    if lang in BAD_DOMINANT:
        return 1, (
            f".gitattributes exists but dominant language is still {lang}. "
            "Mark scaffolding with linguist-vendored / linguist-documentation "
            "so product code leads language stats (see templates/linguist/child.gitattributes)."
        )
    return 0, f"OK: linguist attribution patterns present (dominant={lang or 'n/a'})"


def main(argv: list[str] | None = None) -> int:
    args = argv if argv is not None else sys.argv[1:]
    code, msg = check(Path(args[0] if args else ".").resolve())
    print(msg)
    return code


if __name__ == "__main__":
    raise SystemExit(main())
