# Pack FOSS Cursor components into dist/cursor-plugin/ (standard plugin layout).
# Idempotent. Does not modify live .cursor/. Output is gitignored.
$ErrorActionPreference = 'Stop'
$Root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$Out = Join-Path $Root 'dist\cursor-plugin'
$Src = Join-Path $Root '.cursor'

if (Test-Path -LiteralPath $Out) {
  Remove-Item -LiteralPath $Out -Recurse -Force
}
New-Item -ItemType Directory -Path (Join-Path $Out '.cursor-plugin') -Force | Out-Null
foreach ($d in @('rules', 'skills', 'agents', 'commands', 'hooks')) {
  New-Item -ItemType Directory -Path (Join-Path $Out $d) -Force | Out-Null
}

Copy-Item -LiteralPath (Join-Path $Root '.cursor-plugin\plugin.json') -Destination (Join-Path $Out '.cursor-plugin\plugin.json') -Force

function Copy-Tree([string]$From, [string]$To) {
  if (Test-Path -LiteralPath $From) {
    Copy-Item -Path (Join-Path $From '*') -Destination $To -Recurse -Force -ErrorAction SilentlyContinue
  }
}

Copy-Tree (Join-Path $Src 'rules') (Join-Path $Out 'rules')
Copy-Tree (Join-Path $Src 'skills') (Join-Path $Out 'skills')
Copy-Tree (Join-Path $Src 'agents') (Join-Path $Out 'agents')
Copy-Tree (Join-Path $Src 'commands') (Join-Path $Out 'commands')

Copy-Item -LiteralPath (Join-Path $Src 'hooks.json') -Destination (Join-Path $Out 'hooks.json') -Force
Get-ChildItem -LiteralPath (Join-Path $Src 'hooks') -File -ErrorAction SilentlyContinue |
  Where-Object { $_.Extension -eq '.py' -or $_.Name -eq 'shell-denylist.txt' } |
  ForEach-Object { Copy-Item -LiteralPath $_.FullName -Destination (Join-Path $Out 'hooks') -Force }

foreach ($name in @('permissions.json', 'worktrees.json', 'setup-worktree-unix.sh', 'setup-worktree-windows.ps1')) {
  $p = Join-Path $Src $name
  if (Test-Path -LiteralPath $p) {
    Copy-Item -LiteralPath $p -Destination (Join-Path $Out $name) -Force
  }
}

Get-ChildItem -LiteralPath $Out -Recurse -File -ErrorAction SilentlyContinue |
  Where-Object { $_.Name -like '*.commercial.example' -or $_.Name -eq 'BUGBOT.md' -or $_.Name -eq 'environment.json' } |
  Remove-Item -Force -ErrorAction SilentlyContinue

Write-Host "OK packed FOSS plugin → $Out"
Write-Host 'Local test: symlink this directory to ~/.cursor/plugins/local/agent-project-bootstrap (not the repo root)'
