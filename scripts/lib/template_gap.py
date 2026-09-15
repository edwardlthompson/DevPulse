"""Classify template vs child gaps. Plan-only; Sacred never in apply."""
from __future__ import annotations

import json
import shutil
import subprocess
import sys
from fnmatch import fnmatch
from pathlib import Path

TIMEOUT = 10
ROOT = Path(__file__).resolve().parents[2]
DEFAULT_UPSTREAM = "edwardlthompson/agent-project-bootstrap"


def load_json(path: Path) -> dict:
    if not path.is_file():
        return {}
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError:
        return {}
    return data if isinstance(data, dict) else {}


def child_stack(root: Path) -> str:
    sel = load_json(root / ".cursor/stack-selection.json")
    if sel.get("stack"):
        return str(sel["stack"]).lower()
    return str(load_json(root / "bootstrap.config.json").get("stack") or "").lower()


def matches(path: str, glob: str) -> bool:
    norm = path.replace("\\", "/")
    if norm.startswith("./"):
        norm = norm[2:]
    pat = glob.replace("\\", "/").rstrip("/")
    if glob.endswith("/"):
        return norm == pat or norm.startswith(pat + "/")
    return norm == pat or fnmatch(norm, pat)


def classify(path: str, rules: list) -> str:
    for policy in ("sacred", "mixed", "canon"):
        for rule in rules:
            if rule.get("policy") == policy and matches(path, str(rule.get("glob") or "")):
                return policy
    return "mixed"


def recommended_apply(items: list) -> list[str]:
    return [i["path"] for i in items if i.get("policy") == "canon"]


def gh_json(url: str) -> tuple[dict, str]:
    gh = shutil.which("gh")
    if not gh:
        return {}, "gh missing"
    try:
        proc = subprocess.run(
            [gh, "api", url], capture_output=True, text=True, timeout=TIMEOUT, check=False
        )
    except (OSError, subprocess.TimeoutExpired):
        return {}, "timeout or unreachable"
    if proc.returncode != 0:
        return {}, "gh failed"
    try:
        data = json.loads(proc.stdout or "{}")
    except json.JSONDecodeError:
        return {}, "gh non-JSON"
    return data if isinstance(data, dict) else {}, ""


def latest_release(upstream: str) -> tuple[str, str]:
    data, err = gh_json(f"repos/{upstream}/releases/latest")
    return str(data.get("tag_name") or "").lstrip("v"), err


def gh_compare(upstream: str, current: str, latest: str) -> tuple[list[str], str]:
    if not current or not latest or current == latest:
        return [], "" if current == latest else "skip: missing version"
    data, err = gh_json(f"repos/{upstream}/compare/v{current}...v{latest}")
    if err:
        return [], err
    files = data.get("files") or []
    return [str(f.get("filename") or "") for f in files if isinstance(f, dict) and f.get("filename")], ""


def exists_glob(root: Path, pattern: str) -> bool:
    return any(root.glob(pattern)) if any(c in pattern for c in "*?[") else (root / pattern).exists()


def feature_gaps(root: Path, catalog: dict, stack: str) -> list[dict]:
    gaps: list[dict] = []
    for feat in catalog.get("features") or []:
        stacks = [s.lower() for s in feat.get("stacks") or []]
        if stack and stack not in ("multi", "none") and stack not in stacks:
            continue
        detect = feat.get("detect") or {}
        keys = list(detect) if stack in ("multi", "none", "") else [stack]
        found = any(exists_glob(root, pat) for key in keys for pat in detect.get(key) or [])
        if not found:
            gaps.append({"id": feat.get("id"), "title": feat.get("title"),
                         "spec": feat.get("spec"), "action": "feature"})
    return gaps


def report(root: Path, *, compare=None, latest_fn=None) -> dict:
    ver_path = root / ".template-version"
    version = ver_path.read_text(encoding="utf-8").strip() if ver_path.is_file() else ""
    skip = []
    if not version:
        skip.append("missing .template-version; run check-template-updates")
    if not (root / ".template-update.json").is_file():
        skip.append("missing .template-update.json; run check-template-updates")
    rules = load_json(root / "schemas/golden-path/upgrade-policy.json").get("rules") or []
    catalog = load_json(root / "schemas/golden-path/feature-catalog.json")
    stack = child_stack(root)
    upstream = str(load_json(root / ".template-update.json").get("upstream") or DEFAULT_UPSTREAM)
    latest, rel_err = (latest_fn or latest_release)(upstream) if version else ("", "no version")
    files, cmp_err = (
        (compare or gh_compare)(upstream, version, latest) if version and latest
        else ([], rel_err or "offline: no file diff")
    )
    classified = [{"path": p, "policy": classify(p, rules)} for p in files]
    optional = [
        {
            "id": name,
            "present": (root / "examples" / name).is_dir(),
            "required": stack == name,
            "docs": "docs/OPTIONAL_STACKS.md",
        }
        for name in ("rust", "go", "lightroom", "blender")
    ]
    return {
        "ok": not skip, "current": version, "latest": latest, "stack": stack,
        "upstream": upstream,
        "skip": skip, "warning": cmp_err or rel_err or "", "files": classified,
        "apply": recommended_apply(classified), "features": feature_gaps(root, catalog, stack),
        "optional_stacks": optional,
    }


def main() -> int:
    print(json.dumps(report(ROOT), indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
