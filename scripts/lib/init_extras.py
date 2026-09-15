"""Optional init writers: FUNDING.yml, GitHub About topics, Android SDK local.properties."""
from __future__ import annotations

import os
import re
from pathlib import Path

PLACEHOLDER_RE = re.compile(r"\[INSERT[^\]]*\]", re.I)


def donation_url_usable(url: str) -> bool:
    text = (url or "").strip()
    if not text or PLACEHOLDER_RE.search(text):
        return False
    return text.startswith(("https://", "http://"))


def write_funding_yml(root: Path, url: str) -> Path | None:
    if not donation_url_usable(url):
        return None
    dest = root / ".github" / "FUNDING.yml"
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(f"custom:\n  - {url.strip()}\n", encoding="utf-8")
    return dest


def merge_topics(about_text: str, topics: list[str]) -> str:
    clean = [t.strip().lower().replace(" ", "-") for t in topics if t.strip()]
    if not clean:
        return about_text
    line = ", ".join(clean)
    if "## Topics" in about_text:
        before, rest = about_text.split("## Topics", 1)
        after = rest.split("\n", 1)[1] if "\n" in rest else ""
        # keep remainder after the first paragraph
        parts = after.split("\n\n", 1)
        tail = parts[1] if len(parts) > 1 else ""
        mid = f"## Topics\n\n{line}\n\nSuggested for GitHub discoverability (Settings → About).\n"
        return before + mid + (("\n" + tail) if tail else "")
    return about_text.rstrip() + f"\n\n## Topics\n\n{line}\n"


def write_topics(root: Path, topics: list[str]) -> Path | None:
    path = root / "docs" / "GITHUB_ABOUT.md"
    if not path.is_file() or not topics:
        return None
    text = merge_topics(path.read_text(encoding="utf-8"), topics)
    path.write_text(text, encoding="utf-8")
    return path


def gh_topics_command(topics: list[str]) -> str:
    clean = [t.strip().lower().replace(" ", "-") for t in topics if t.strip()]
    if not clean:
        return ""
    joined = ",".join(clean)
    return f"gh repo edit --add-topic {joined}"


def detect_android_sdk(home: Path | None = None, env: dict[str, str] | None = None) -> Path | None:
    """Locate an Android SDK on Linux/macOS (ANDROID_HOME or common user paths)."""
    environ = env if env is not None else os.environ
    for key in ("ANDROID_HOME", "ANDROID_SDK_ROOT"):
        raw = (environ.get(key) or "").strip()
        if raw:
            path = Path(raw).expanduser()
            if (path / "platform-tools").is_dir():
                return path
    base = home if home is not None else Path.home()
    for candidate in (base / "Android" / "Sdk", base / ".local" / "android"):
        if (candidate / "platform-tools").is_dir():
            return candidate
    return None


def write_android_local_properties(root: Path, sdk: Path | None = None) -> Path | None:
    """Write gitignored examples/android/local.properties when SDK is detected."""
    android = root / "examples" / "android"
    if not android.is_dir():
        return None
    resolved = sdk if sdk is not None else detect_android_sdk()
    if resolved is None:
        return None
    dest = android / "local.properties"
    sdk_dir = str(resolved).replace("\\", "\\\\").replace(":", "\\:")
    dest.write_text(f"sdk.dir={sdk_dir}\n", encoding="utf-8")
    return dest
