"""Local-first dependency update policy (dry-run default, Kotlin CodeQL ceiling)."""
from __future__ import annotations

import os
import re
import shutil
from pathlib import Path

UPD_SPEC = "upd-cli==0.6.2"
DEFAULT_TIMEOUT = 120
KOTLIN_BLOCK = (2, 3, 30)
GRADLE_FALLBACK = "Gradle: Dependabot backup or enable depsonar MCP."
KOTLIN_RE = re.compile(
    r"org\.jetbrains\.kotlin[^\n]*version\s+\"(\d+)\.(\d+)\.(\d+)\""
)
LANG_MARKERS: dict[str, tuple[str, ...]] = {
    "node": ("examples/web/package.json", "examples/node/package.json"),
    "python": ("examples/python/pyproject.toml",),
    "actions": (".github/workflows",),
    "pre-commit": (".pre-commit-config.yaml",),
}


def timeout_seconds() -> int:
    raw = os.environ.get("UPDATE_DEPS_TIMEOUT", str(DEFAULT_TIMEOUT)).strip()
    try:
        value = int(raw)
    except ValueError as exc:
        raise SystemExit(f"invalid UPDATE_DEPS_TIMEOUT: {raw}") from exc
    if value <= 0:
        raise SystemExit("UPDATE_DEPS_TIMEOUT must be > 0")
    return value


def parse_kotlin_versions(text: str) -> list[tuple[int, int, int]]:
    return [(int(a), int(b), int(c)) for a, b, c in KOTLIN_RE.findall(text)]


def kotlin_guard_error(root: Path) -> str | None:
    android = root / "examples" / "android"
    if not android.is_dir():
        return None
    blocked: list[str] = []
    for path in android.rglob("*.gradle.kts"):
        versions = parse_kotlin_versions(path.read_text(encoding="utf-8"))
        for ver in versions:
            if ver >= KOTLIN_BLOCK:
                blocked.append(f"{path.as_posix()}: {'.'.join(map(str, ver))}")
    if not blocked:
        return None
    return "Kotlin >= 2.3.30 blocked (CodeQL): " + "; ".join(blocked)


PLUGIN_RE = re.compile(r'id\("([^"]+)"\)\s+version\s+"([^"]+)"')


def gradle_pin_lines(root: Path) -> list[str]:
    path = root / "examples" / "android" / "build.gradle.kts"
    if not path.is_file():
        return []
    return [f"{name}={ver}" for name, ver in PLUGIN_RE.findall(path.read_text(encoding="utf-8"))]


def gradle_fallback_message(root: Path) -> str | None:
    if not (root / "examples" / "android" / "build.gradle.kts").is_file():
        return None
    pins = gradle_pin_lines(root)
    extra = f" Pins: {', '.join(pins)}." if pins else ""
    return GRADLE_FALLBACK + extra


def discover_langs(root: Path) -> list[str]:
    found: list[str] = []
    for lang, markers in LANG_MARKERS.items():
        for rel in markers:
            if (root / rel).exists():
                found.append(lang)
                break
    return found


def upd_base() -> list[str]:
    return ["uvx", "--from", UPD_SPEC, "upd"]


def upd_argv(mode: str, langs: list[str]) -> list[str]:
    argv = upd_base() + ["--max-bump", "minor"]
    for lang in langs:
        argv.extend(["--lang", lang])
    if "actions" in langs:
        argv.append("--update-action-shas")
    if mode == "apply":
        argv.extend(["--apply", "--lock"])
    else:
        argv.append("--dry-run")
    return argv


def release_please_dry_argv(repo_url: str) -> list[str]:
    return [
        "npx", "--yes", "release-please@16", "release-pr", "--dry-run",
        "--repo-url", repo_url,
        "--config-file", "release-please-config.json",
        "--manifest-file", ".release-please-manifest.json",
    ]


def with_github_token(argv: list[str], token: str) -> list[str]:
    if token:
        return argv + ["--token", token]
    return list(argv)


def pre_release_steps(local: bool) -> list[str]:
    steps = ["feature-gate", "audit-deps" if local else "check-security-triage",
             "version", "license"]
    if not local:
        steps.append("branch-protection")
    return steps


def audit_jobs(root: Path) -> list[tuple[list[str], Path | None]]:
    jobs: list[tuple[list[str], Path | None]] = []
    if shutil.which("uvx"):
        jobs.append((upd_base() + ["audit", "--check"], None))
    for sub in ("examples/web", "examples/node"):
        if (root / sub / "package-lock.json").is_file() and shutil.which("npm"):
            jobs.append((["npm", "audit", "--audit-level=high"], root / sub))
    py_dir = root / "examples" / "python"
    if (py_dir / "uv.lock").is_file() and shutil.which("uvx"):
        jobs.append((["uvx", "pip-audit"], py_dir))
    if shutil.which("trivy"):
        jobs.append(
            (["trivy", "fs", "--severity", "HIGH,CRITICAL", "--exit-code", "1", "."], root)
        )
    if shutil.which("osv-scanner"):
        jobs.append((["osv-scanner", "-r", "."], root))
    return jobs
