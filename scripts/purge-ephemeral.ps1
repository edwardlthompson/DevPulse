param(
  [switch]$Apply
)

$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root

if (-not (Test-Path ".git")) {
  Write-Error "Not a git repository"
  exit 1
}

if ($Apply) {
  Write-Host "Applying purge (ignored untracked files only)..."
  git clean -fdX
  Write-Host "Purge complete."
} else {
  Write-Host "Dry-run — would remove these ignored untracked paths:"
  $preview = git clean -fdXn
  if (-not $preview) {
    Write-Host "Nothing to purge."
  } else {
    $preview
    Write-Host ""
    Write-Host "Tip: run with -Apply to reclaim disk space."
  }
}
