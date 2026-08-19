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
    npm ci
    if ($LASTEXITCODE -eq 0) { Write-Host "OK npm ci in $Dir" } else { Write-Host "SKIP npm ci failed in $Dir (non-fatal)" }
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

function Install-GradleDir([string]$Dir) {
  $gw = Join-Path $Dir 'gradlew.bat'
  if (-not (Test-Path -LiteralPath $gw)) {
    Write-Host "SKIP gradle in $Dir (no gradlew.bat)"
    return
  }
  Push-Location $Dir
  try {
    & .\gradlew.bat --version
    if ($LASTEXITCODE -eq 0) { Write-Host "OK gradle wrapper in $Dir" } else { Write-Host "SKIP gradle failed in $Dir (non-fatal)" }
  } catch {
    Write-Host "SKIP gradle failed in $Dir (non-fatal)"
  } finally {
    Pop-Location
  }
}

switch ($Stack) {
  'web' { Install-NpmDir (Join-Path $Wt 'examples\web') }
  'node' { Install-NpmDir (Join-Path $Wt 'examples\node') }
  'python' { Install-UvDir (Join-Path $Wt 'examples\python') }
  'android' { Install-GradleDir (Join-Path $Wt 'examples\android') }
  Default {
    Install-NpmDir (Join-Path $Wt 'examples\web')
    Install-NpmDir (Join-Path $Wt 'examples\node')
    Install-UvDir (Join-Path $Wt 'examples\python')
    Install-GradleDir (Join-Path $Wt 'examples\android')
  }
}

Write-Host 'worktree setup complete (fail-soft)'
exit 0
