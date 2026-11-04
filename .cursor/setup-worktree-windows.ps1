# Cursor native worktree setup (fail-soft).
# Allowlist: copy *.env.example only — never .env / credentials.
# Exit 0 on missing stack/tools; exit 1 only if stack-selection.json exists but is corrupt.

$ErrorActionPreference = 'Continue'
$RootSrc = $env:ROOT_WORKTREE_PATH
$Wt = (Get-Location).Path
$rootLabel = if ($RootSrc) { $RootSrc } else { 'unset' }
Write-Host "worktree setup: cwd=$Wt root=$rootLabel"

function Copy-EnvExamples {
  param([string]$Src, [string]$Dest)
  if (-not (Test-Path -LiteralPath $Src -PathType Container)) { return }
  $rootExample = Join-Path $Src '.env.example'
  $destExample = Join-Path $Dest '.env.example'
  if ((Test-Path -LiteralPath $rootExample) -and -not (Test-Path -LiteralPath $destExample)) {
    try {
      Copy-Item -LiteralPath $rootExample -Destination $destExample -Force
      Write-Host 'OK copied .env.example'
    } catch {
      Write-Host 'SKIP copy .env.example'
    }
  }
  Get-ChildItem -LiteralPath $Src -Recurse -Filter '*.env.example' -File -ErrorAction SilentlyContinue | ForEach-Object {
    $rel = $_.FullName.Substring($Src.Length).TrimStart('\', '/')
    if ($rel -match '(^|[\\/])\.env$' -or $rel -match '\.env\.local' -or $rel -match 'credentials') { return }
    $target = Join-Path $Dest $rel
    $targetDir = Split-Path -Parent $target
    if (-not (Test-Path -LiteralPath $targetDir)) {
      New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
    }
    if (-not (Test-Path -LiteralPath $target)) {
      try {
        Copy-Item -LiteralPath $_.FullName -Destination $target -Force
        Write-Host "OK copied $rel"
      } catch {
        Write-Host "SKIP copy $rel"
      }
    }
  }
}

if ($RootSrc -and (Test-Path -LiteralPath $RootSrc -PathType Container)) {
  Copy-EnvExamples -Src $RootSrc -Dest $Wt
} else {
  Write-Host 'SKIP env examples (ROOT_WORKTREE_PATH unset or missing)'
}

$env:ROOT_WORKTREE_PATH = $RootSrc
function Resolve-PythonExe {
  $pyLauncher = Get-Command py -ErrorAction SilentlyContinue
  if ($pyLauncher) {
    try {
      $exe = & py -3 -c "import sys; print(sys.executable)" 2>$null
      if ($exe -and ($exe -notmatch 'WindowsApps')) { return $exe }
    } catch {}
  }
  foreach ($name in @('python3', 'python')) {
    $cmd = Get-Command $name -ErrorAction SilentlyContinue
    if ($cmd -and $cmd.Source -and ($cmd.Source -notmatch 'WindowsApps')) {
      return $cmd.Source
    }
  }
  return 'python3'
}
$Python = Resolve-PythonExe
& $Python -c @"
from pathlib import Path
import os, sys
root = os.environ.get('ROOT_WORKTREE_PATH') or ''
try:
    ok = bool(root) and Path(root).is_dir() and Path(root).resolve() != Path('.').resolve()
except OSError:
    ok = False
raise SystemExit(0 if ok else 1)
"@
if ($LASTEXITCODE -ne 0) {
  Write-Host 'SKIP stack install (not a Cursor worktree; set ROOT_WORKTREE_PATH to the primary repo)'
  exit 0
}

$StackFile = Join-Path $Wt '.cursor\stack-selection.json'
if (-not (Test-Path -LiteralPath $StackFile) -and $RootSrc) {
  $alt = Join-Path $RootSrc '.cursor\stack-selection.json'
  if (Test-Path -LiteralPath $alt) { $StackFile = $alt }
}

if (-not (Test-Path -LiteralPath $StackFile)) {
  Write-Host 'SKIP stack install (no stack-selection.json)'
  exit 0
}

try {
  $stackData = Get-Content -LiteralPath $StackFile -Raw -Encoding UTF8 | ConvertFrom-Json
} catch {
  Write-Error "ERROR corrupt stack-selection.json: $_"
  exit 1
}

$Stack = if ($stackData.stack) { [string]$stackData.stack } else { 'multi' }
Write-Host "stack=$Stack"

function Test-Cmd([string]$Name) {
  return [bool](Get-Command $Name -ErrorAction SilentlyContinue)
}

function Install-NpmDir([string]$Dir) {
  if (-not (Test-Path -LiteralPath (Join-Path $Dir 'package.json'))) {
    Write-Host "SKIP npm in $Dir (no package.json)"
    return
  }
  if (-not (Test-Cmd 'npm')) {
    Write-Host "SKIP npm ci in $Dir (npm not on PATH)"
    return
  }
  Push-Location $Dir
  try {
    npm ci --prefer-offline --no-audit --no-fund
    if ($LASTEXITCODE -eq 0) {
      Write-Host "OK npm ci in $Dir"
    } else {
      Write-Host "WARN npm ci --prefer-offline failed in $Dir; retry without offline"
      npm ci --no-audit --no-fund
      if ($LASTEXITCODE -eq 0) { Write-Host "OK npm ci in $Dir" } else { Write-Host "SKIP npm ci failed in $Dir (non-fatal)" }
    }
  } catch {
    Write-Host "SKIP npm ci failed in $Dir (non-fatal)"
  } finally {
    Pop-Location
  }
}

function Install-UvDir([string]$Dir) {
  if (-not (Test-Path -LiteralPath (Join-Path $Dir 'pyproject.toml'))) {
    Write-Host "SKIP uv in $Dir (no pyproject.toml)"
    return
  }
  if (-not (Test-Cmd 'uv')) {
    Write-Host "SKIP uv sync in $Dir (uv not on PATH)"
    return
  }
  Push-Location $Dir
  try {
    uv sync
    if ($LASTEXITCODE -eq 0) { Write-Host "OK uv sync in $Dir" } else { Write-Host "SKIP uv sync failed in $Dir (non-fatal)" }
  } catch {
    Write-Host "SKIP uv sync failed in $Dir (non-fatal)"
  } finally {
    Pop-Location
  }
}

function Resolve-GradleOfflinePy {
  foreach ($base in @($Wt, $RootSrc)) {
    if (-not $base) { continue }
    $candidate = Join-Path $base 'scripts\lib\gradle_offline.py'
    if (Test-Path -LiteralPath $candidate) { return $candidate }
  }
  return $null
}

function Install-GradleDir([string]$Dir) {
  $gw = Join-Path $Dir 'gradlew.bat'
  if (-not (Test-Path -LiteralPath $gw)) {
    Write-Host "SKIP gradle in $Dir (no gradlew.bat)"
    return
  }
  $helper = Resolve-GradleOfflinePy
  $offline = $false
  if ($helper -and $RootSrc) {
    $extra = & $Python $helper --args --root $RootSrc 2>$null
    if ($extra -match '--offline') { $offline = $true }
  }
  Push-Location $Dir
  try {
    if ($offline) {
      & .\gradlew.bat --offline --version
      if ($LASTEXITCODE -eq 0) {
        Write-Host "OK gradle wrapper in $Dir (offline)"
        return
      }
      Write-Host "WARN gradle --offline failed in $Dir; retry online"
    }
    & .\gradlew.bat --version
    if ($LASTEXITCODE -eq 0) {
      if ($helper -and $RootSrc) {
        & $Python $helper --mark --root $RootSrc 2>$null
      }
      Write-Host "OK gradle wrapper in $Dir"
    } else {
      Write-Host "SKIP gradle failed in $Dir (non-fatal)"
    }
  } catch {
    Write-Host "SKIP gradle failed in $Dir (non-fatal)"
  } finally {
    Pop-Location
  }
}

function Link-PlatformTools {
  $dest = Join-Path $Wt '.cursor\platform-tools'
  $sdk = $null
  foreach ($key in @('ANDROID_HOME', 'ANDROID_SDK_ROOT')) {
    $val = [Environment]::GetEnvironmentVariable($key)
    if ($val -and (Test-Path -LiteralPath (Join-Path $val 'platform-tools') -PathType Container)) {
      $sdk = Join-Path $val 'platform-tools'
      break
    }
  }
  if (-not $sdk) {
    $local = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools'
    if (Test-Path -LiteralPath $local -PathType Container) { $sdk = $local }
  }
  if (-not $sdk) {
    Write-Host 'SKIP platform-tools symlink (SDK not found)'
    return
  }
  $cursorDir = Join-Path $Wt '.cursor'
  if (-not (Test-Path -LiteralPath $cursorDir)) {
    New-Item -ItemType Directory -Path $cursorDir -Force | Out-Null
  }
  if (Test-Path -LiteralPath $dest) {
    Write-Host "OK platform-tools already present at $dest"
    return
  }
  try {
    New-Item -ItemType SymbolicLink -Path $dest -Target $sdk -ErrorAction Stop | Out-Null
    Write-Host "OK linked platform-tools -> $sdk"
  } catch {
    try {
      cmd /c mklink /J "`"$dest`"" "`"$sdk`"" | Out-Null
      Write-Host "OK junction platform-tools -> $sdk"
    } catch {
      Write-Host 'SKIP platform-tools symlink (non-fatal)'
    }
  }
}

switch ($Stack) {
  'web' { Install-NpmDir (Join-Path $Wt 'examples\web') }
  'node' { Install-NpmDir (Join-Path $Wt 'examples\node') }
  'python' { Install-UvDir (Join-Path $Wt 'examples\python') }
  'android' {
    Link-PlatformTools
    Install-GradleDir (Join-Path $Wt 'examples\android')
  }
  Default {
    Install-NpmDir (Join-Path $Wt 'examples\web')
    Install-NpmDir (Join-Path $Wt 'examples\node')
    Install-UvDir (Join-Path $Wt 'examples\python')
    Link-PlatformTools
    Install-GradleDir (Join-Path $Wt 'examples\android')
  }
}

Write-Host 'worktree setup complete (fail-soft)'
exit 0
