"""Assert Golden Path demo app versions share one source of truth."""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CHANGELOG_VER = re.compile(r"^## \[(\d+\.\d+\.\d+)\]", re.M)


def version_code(version: str) -> int:
    core = version.split("-", 1)[0].split("+", 1)[0]
    parts = core.split(".")
    major = int(parts[0]) if parts and parts[0].isdigit() else 0
    minor = int(parts[1]) if len(parts) > 1 and parts[1].isdigit() else 0
    patch = int(parts[2]) if len(parts) > 2 and parts[2].isdigit() else 0
    return major * 10000 + minor * 100 + patch


def semver_tuple(version: str) -> tuple[int, int, int]:
    core = version.split("-", 1)[0].split("+", 1)[0]
    parts = core.split(".")
    nums = [int(parts[i]) if i < len(parts) and parts[i].isdigit() else 0 for i in range(3)]
    return nums[0], nums[1], nums[2]


def load_sot(base: Path) -> tuple[str, int] | list[str]:
    sot_path = base / "schemas/golden-path/app-version.json"
    if not sot_path.is_file():
        return ["MISSING: schemas/golden-path/app-version.json"]
    data = json.loads(sot_path.read_text(encoding="utf-8"))
    expected_raw = data.get("version")
    if not isinstance(expected_raw, str) or not expected_raw.strip():
        return ["app-version.json version must be a non-empty string"]
    expected = expected_raw.strip()
    code = data.get("versionCode")
    if not isinstance(code, int) or code <= 0:
        return ["app-version.json versionCode must be a positive int"]
    derived = version_code(expected)
    if code != derived:
        return [f"app-version.json versionCode {code} != derived {derived} from {expected}"]
    return expected, code


def check(root: Path | None = None) -> list[str]:
    base = root or ROOT
    loaded = load_sot(base)
    if isinstance(loaded, list):
        return loaded
    expected, _code = loaded
    errors: list[str] = []

    web = base / "examples/web/package.json"
    if web.is_file():
        got = str(json.loads(web.read_text(encoding="utf-8")).get("version", "")).strip()
        if got != expected:
            errors.append(f"examples/web/package.json version {got!r} != app-version.json {expected!r}")

    pyproject = base / "examples/python/pyproject.toml"
    if pyproject.is_file():
        match = re.search(r'(?m)^version\s*=\s*"([^"]+)"', pyproject.read_text(encoding="utf-8"))
        py_ver = match.group(1) if match else None
        if py_ver != expected:
            errors.append(
                f"examples/python/pyproject.toml version {py_ver!r} != app-version.json {expected!r}"
            )

    about = base / "examples/python/src/hello/about.py"
    if about.is_file():
        text = about.read_text(encoding="utf-8")
        if "importlib.metadata" not in text and 'version("golden-path-python")' not in text:
            match = re.search(r'APP_VERSION\s*=\s*"([^"]+)"', text)
            got = match.group(1) if match else None
            if got != expected:
                errors.append(
                    f"examples/python/src/hello/about.py APP_VERSION {got!r} != "
                    f"app-version.json {expected!r}"
                )

    changelog = base / "CHANGELOG.md"
    if changelog.is_file():
        match = CHANGELOG_VER.search(changelog.read_text(encoding="utf-8"))
        if match and semver_tuple(expected) < semver_tuple(match.group(1)):
            errors.append(
                f"app-version.json {expected!r} is behind CHANGELOG {match.group(1)!r}"
            )

    gradle = base / "examples/android/app/build.gradle.kts"
    if gradle.is_file():
        text = gradle.read_text(encoding="utf-8")
        if "app-version.json" not in text or "readGoldenPathAppVersion" not in text:
            errors.append(
                "examples/android/app/build.gradle.kts must read versionName via "
                "readGoldenPathAppVersion() from schemas/golden-path/app-version.json"
            )
        if "readGoldenPathVersionCode" not in text or '"versionCode"' not in text:
            errors.append(
                "examples/android/app/build.gradle.kts must read versionCode from "
                "schemas/golden-path/app-version.json"
            )
        if 'getByName("release")' in text:
            errors.append(
                'examples/android/app/build.gradle.kts must not use signingConfigs.getByName("release")'
            )
        if "maybeCreate(" not in text or "findByName(" not in text:
            errors.append(
                "examples/android/app/build.gradle.kts must use signingConfigs.maybeCreate/findByName"
            )

    return errors


def main() -> int:
    errors = check()
    if errors:
        print("\n".join(errors))
        return 1
    version, code = load_sot(ROOT)  # type: ignore[misc]
    print(f"Golden Path app version sync OK ({version} / {code})")
    return 0


if __name__ == "__main__":
    sys.exit(main())
