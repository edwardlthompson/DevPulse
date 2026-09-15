"""GlitchTip/Bugsink crash-inbox stub stays disabled until a HUMAN DPIA."""
from __future__ import annotations

import json
from pathlib import Path

EXAMPLE = Path("schemas") / "golden-path" / "crash-inbox.example.json"
DOC = Path("docs") / "CRASH_INBOX.md"
ALLOWED_PROVIDERS = frozenset({"none", "glitchtip", "bugsink"})
FORBIDDEN_SDKS = (
    "io.sentry",
    "com.google.firebase:firebase-crashlytics",
    "@sentry/browser",
    "@sentry/react",
    "glitchtip-sdk",
    "bugsink",
)
MANIFESTS = (
    "examples/web/package.json",
    "examples/node/package.json",
    "examples/android/app/build.gradle.kts",
    "examples/python/pyproject.toml",
)
DOC_NEEDLES = ("glitchtip", "bugsink", "dpia", "not a live", "disabled")


def _as_dict(raw: object) -> dict:
    return raw if isinstance(raw, dict) else {}


def check_stub(data: dict, rel: str) -> list[str]:
    errors: list[str] = []
    if data.get("enabled") is True:
        errors.append(f"{rel}: crash inbox enabled; DPIA required")
    provider = str(data.get("provider") or "none")
    if provider not in ALLOWED_PROVIDERS:
        errors.append(f"{rel}: provider must be none|glitchtip|bugsink")
    if str(data.get("dsn") or "").strip() or str(data.get("endpoint") or "").strip():
        errors.append(f"{rel}: stub must not ship a live DSN or endpoint")
    return errors


def check_repo(root: Path) -> list[str]:
    errors: list[str] = []
    example = root / EXAMPLE
    if not example.is_file():
        return [f"MISSING: {EXAMPLE.as_posix()}"]
    errors.extend(check_stub(json.loads(example.read_text(encoding="utf-8")), EXAMPLE.as_posix()))
    for name in ("bootstrap.config.json", "bootstrap.config.json.example"):
        path = root / name
        if not path.is_file():
            continue
        data = _as_dict(json.loads(path.read_text(encoding="utf-8")))
        inbox = data.get("crash_inbox")
        if inbox:
            errors.extend(check_stub(_as_dict(inbox), name))
        if _as_dict(data.get("crash_proxy")).get("enabled") is True:
            errors.append(f"{name}: crash_proxy.enabled must stay false")
    doc = root / DOC
    if not doc.is_file():
        errors.append(f"MISSING: {DOC.as_posix()}")
    else:
        text = doc.read_text(encoding="utf-8").lower()
        for needle in DOC_NEEDLES:
            if needle not in text:
                errors.append(f"{DOC.as_posix()} must mention {needle}")
    for rel in MANIFESTS:
        path = root / rel
        if not path.is_file():
            continue
        text = path.read_text(encoding="utf-8")
        for sdk in FORBIDDEN_SDKS:
            if sdk in text:
                errors.append(f"{rel}: FOSS path must not depend on {sdk}")
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("Crash inbox stub check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("Crash inbox stub stays disabled")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
