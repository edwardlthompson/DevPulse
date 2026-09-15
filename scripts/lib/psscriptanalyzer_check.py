"""PSScriptAnalyzer -Severity Error on scripts/*.ps1 + worktree setup. Skip if missing."""
from __future__ import annotations

import os
import shutil
import subprocess
from pathlib import Path

from local_resources import in_ci

ANALYZE_PS1 = r"""
$ErrorActionPreference = 'Stop'
if (-not (Get-Module -ListAvailable -Name PSScriptAnalyzer)) {
  Write-Output 'MISSING_MODULE'
  exit 2
}
Import-Module PSScriptAnalyzer
$fail = 0
$paths = @(Get-ChildItem -LiteralPath 'scripts' -Filter '*.ps1' -File -ErrorAction SilentlyContinue)
$wt = Join-Path '.cursor' 'setup-worktree-windows.ps1'
if (Test-Path -LiteralPath $wt) { $paths += Get-Item -LiteralPath $wt }
foreach ($p in $paths) {
  $hits = Invoke-ScriptAnalyzer -Path $p.FullName -Severity Error
  if ($hits) {
    $hits | ForEach-Object { Write-Output ("{0}:{1} {2} {3}" -f $p.Name, $_.Line, $_.RuleName, $_.Message) }
    $fail = 1
  }
}
if ($fail -eq 0) { Write-Output ("OK: PSScriptAnalyzer ({0} scripts, -Severity Error)" -f $paths.Count) }
exit $fail
"""


def require_tools() -> bool:
    raw = os.environ.get("REQUIRE_PSSA", "").strip().lower()
    return raw in {"1", "true", "yes"} or in_ci()


def _shell() -> str | None:
    return shutil.which("pwsh") or shutil.which("powershell")


def check_psscriptanalyzer(root: Path, *, shell: str | None | object = None) -> int:
    exe = _shell() if shell is None else shell
    if not exe:
        if require_tools():
            print("FAIL PSScriptAnalyzer: pwsh/powershell missing")
            return 1
        print("SKIP PSScriptAnalyzer (no pwsh/powershell; local --quick)")
        return 0
    try:
        proc = subprocess.run(
            [str(exe), "-NoProfile", "-NonInteractive", "-Command", ANALYZE_PS1],
            cwd=root,
            capture_output=True,
            text=True,
            timeout=180,
            check=False,
        )
    except (OSError, subprocess.TimeoutExpired) as exc:
        print(f"FAIL PSScriptAnalyzer: {exc}")
        return 1
    out = (proc.stdout or "") + (proc.stderr or "")
    if out.strip():
        print(out.rstrip())
    if proc.returncode == 2 or "MISSING_MODULE" in out:
        if require_tools():
            print("FAIL PSScriptAnalyzer module missing")
            return 1
        print("SKIP PSScriptAnalyzer (module not installed; local --quick)")
        return 0
    if proc.returncode != 0:
        print("FAIL: PSScriptAnalyzer")
        return 1
    return 0


def main() -> int:
    return check_psscriptanalyzer(Path.cwd())


if __name__ == "__main__":
    raise SystemExit(main())
