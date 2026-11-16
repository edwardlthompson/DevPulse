"""F-Droid source readiness: version SoT, LICENSE, signing, screenshots."""

from __future__ import annotations

import json
import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
ROOT = Path(__file__).resolve().parent.parent
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from fdroid_screenshots import MIN_BYTES, check_completeness, check_tree  # noqa: E402
from golden_path_app_version import check, version_code  # noqa: E402


class VersionSotTests(unittest.TestCase):
    def test_formula_matches_shipped_release(self) -> None:
        self.assertEqual(version_code("1.0.0"), 10000)
        self.assertEqual(version_code("0.38.3"), 3803)

    def test_repo_matches_changelog_and_gradle(self) -> None:
        self.assertEqual(check(ROOT), [])
        data = json.loads((ROOT / "schemas/golden-path/app-version.json").read_text(encoding="utf-8"))
        self.assertEqual(data["version"], "1.0.0")
        self.assertEqual(version_code(data["version"]), 10000)
        self.assertNotIn("versionCode", data)

    def test_detects_template_reset(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "schemas/golden-path").mkdir(parents=True)
            (root / "schemas/golden-path/app-version.json").write_text(
                json.dumps({"version": "0.1.0"}),
                encoding="utf-8",
            )
            (root / "CHANGELOG.md").write_text("## [Unreleased]\n\n## [1.0.0]\n", encoding="utf-8")
            errors = check(root)
            self.assertTrue(any("behind CHANGELOG" in e for e in errors))

    def test_stale_json_version_code_fails(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "schemas/golden-path").mkdir(parents=True)
            (root / "schemas/golden-path/app-version.json").write_text(
                json.dumps({"version": "1.0.1", "versionCode": 10000}),
                encoding="utf-8",
            )
            errors = check(root)
            self.assertTrue(any("versionCode 10000 != derived 10001" in e for e in errors))


class LicenseAndSigningTests(unittest.TestCase):
    def test_license_is_canonical_gpl3(self) -> None:
        text = (ROOT / "LICENSE").read_text(encoding="utf-8")
        self.assertTrue(text.lstrip().startswith("GNU GENERAL PUBLIC LICENSE"))
        self.assertIn("Version 3, 29 June 2007", text)
        self.assertIn("<one line to give the program", text)
        self.assertNotIn("SPDX-License-Identifier", text.split("GNU GENERAL PUBLIC LICENSE", 1)[0])

    def test_notice_keeps_or_later(self) -> None:
        notice = (ROOT / "NOTICE").read_text(encoding="utf-8")
        self.assertIn("GPL-3.0-or-later", notice)
        self.assertIn("Edward L. Thompson", notice)

    def test_release_signing_is_strip_tolerant(self) -> None:
        text = (ROOT / "examples/android/app/build.gradle.kts").read_text(encoding="utf-8")
        self.assertIn("maybeCreate(", text)
        self.assertIn("findByName(", text)
        self.assertNotIn('getByName("release")', text)


class FastlaneTests(unittest.TestCase):
    def test_nonfreenet_names_play_github_aptoide(self) -> None:
        text = (ROOT / "fastlane/metadata/android/en-US/antifeatures.yml").read_text(encoding="utf-8")
        self.assertIn("NonFreeNet", text)
        self.assertIn("Google Play", text)
        self.assertIn("GitHub", text)
        self.assertIn("Aptoide", text)

    def test_screenshots_are_real(self) -> None:
        self.assertEqual(check_tree(ROOT), [])
        shot = ROOT / "fastlane/metadata/android/en-US/images/phoneScreenshots/screenshot_1.png"
        self.assertGreaterEqual(shot.stat().st_size, MIN_BYTES)
        self.assertEqual(check_completeness(ROOT, submit_ready=True), [])


if __name__ == "__main__":
    unittest.main()
