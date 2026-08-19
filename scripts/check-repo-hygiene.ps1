$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

$errors = 0

function Invoke-Step([scriptblock]$Block) {
  try {
    & $Block
  } catch {
    $script:errors++
  }
}

Invoke-Step { bash scripts/check-tracked-artifacts.sh }
Invoke-Step { bash scripts/check-large-tracked-files.sh }

$required = @("node_modules/", "dist/", ".env", "__pycache__/", "coverage/")
if (Test-Path ".gitignore") {
  $ignore = Get-Content ".gitignore" -Raw
  foreach ($entry in $required) {
    if ($ignore -notlike "*$entry*") {
      Write-Host "MISSING .gitignore entry: $entry"
      $errors++
    }
  }
} else {
  Write-Host "MISSING: .gitignore"
  $errors++
}

foreach ($f in @(".gitattributes", ".editorconfig")) {
  if (-not (Test-Path $f)) {
    Write-Host "MISSING: $f"
    $errors++
  }
}

if ($errors -gt 0) {
  Write-Host "$errors repo hygiene check(s) failed"
  exit 1
}

Write-Host "Repo hygiene checks passed"
