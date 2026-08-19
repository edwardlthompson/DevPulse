# Post-template clone customization helper
param(
    [string]$Stack = "",
    [string]$ProjectName = "",
    [string]$ProjectPurpose = "",
    [string]$Interval = "",
    [string]$ReleaseRepo = "",
    [string]$DonationUrl = "",
    [string]$Topics = "",
    [string]$CodeOwner = "",
    [string]$DistributionTier = "foss",
    [ValidateSet("MIT", "Apache-2.0")]
    [string]$License = "MIT",
    [switch]$SkipPreflight,
    [switch]$StrictPreflight,
    [switch]$Prune,
    [switch]$NoPrune,
    [switch]$NonInteractive,
    [switch]$KeepOptional = $true,
    [switch]$PruneOptional
)

$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $Root

# Python 3.14+ pyrepl on Windows can hang (WinError 123 getheightwidth). See KB-014.
$env:PYTHON_BASIC_REPL = "1"
$env:PYTHONUNBUFFERED = "1"
if (-not $env:PYTHONIOENCODING) { $env:PYTHONIOENCODING = "utf-8" }

if ($PruneOptional) { $KeepOptional = $false }

function Write-Utf8NoBom {
    param([string]$Path, [string]$Content)
    [System.IO.File]::WriteAllText($Path, $Content, (New-Object System.Text.UTF8Encoding $false))
}

function Remove-OptionalStacks {
    if ($KeepOptional) { return }
    @("examples/rust", "examples/go", "examples/lightroom", "modules/rust", "modules/go", "modules/lightroom") | ForEach-Object {
        $target = Join-Path $Root $_
        if (Test-Path $target) { Remove-Item -Recurse -Force $target }
    }
}

function Remove-PrimaryStack {
    param([string]$ActiveStack)
    $toRemove = switch ($ActiveStack) {
        "web" { @("examples/python", "examples/android", "examples/node", "modules/python", "modules/android", "modules/node") }
        "python" { @("examples/web", "examples/android", "examples/node", "modules/web", "modules/android", "modules/node") }
        "android" { @("examples/web", "examples/python", "examples/node", "modules/web", "modules/python", "modules/node") }
        "node" { @("examples/web", "examples/python", "examples/android", "modules/web", "modules/python", "modules/android") }
        default { @() }
    }
    foreach ($item in $toRemove) {
        $target = Join-Path $Root $item
        if (Test-Path $target) { Remove-Item -Recurse -Force $target }
    }
    Remove-OptionalStacks
}

if ($NonInteractive -and (-not $Stack -or -not $ProjectName -or -not $ProjectPurpose)) {
    Write-Error "--NonInteractive requires -Stack, -ProjectName, and -ProjectPurpose"
    exit 1
}

Write-Host "=== agent-project-bootstrap init ===" -ForegroundColor Cyan
Write-Host ""

if (-not $ProjectName -and -not $NonInteractive) { $ProjectName = Read-Host "Project name" }
if (-not $ProjectPurpose -and -not $NonInteractive) { $ProjectPurpose = Read-Host "One-line purpose" }
if (-not $Stack -and -not $NonInteractive) { $Stack = Read-Host "Primary stack (web/python/android/node/multi/none)" }
if (-not $Stack) { $Stack = "none" }
$ValidStacks = @("web", "python", "android", "node", "multi", "none")
if ($ValidStacks -notcontains $Stack) {
    Write-Host "Invalid stack '$Stack'; defaulting to none (keep all examples)."
    $Stack = "none"
}
if (-not $Interval -and -not $NonInteractive) {
    $Interval = Read-Host "Template update check interval (off/daily/weekly/monthly/on_session) [weekly]"
}
if (-not $Interval) { $Interval = "weekly" }

if ($DistributionTier -notin @("foss", "commercial")) {
    Write-Host "Invalid distribution tier '$DistributionTier'; defaulting to foss."
    $DistributionTier = "foss"
}
if (-not $NonInteractive) {
    Write-Host "Distribution tier:"
    Write-Host "  1) FOSS (default) — MIT, no proprietary SDKs"
    Write-Host "  2) Commercial — proprietary SDKs, full Cursor Cloud stack"
    $TierChoice = Read-Host "Choose [1/2]"
    if ($TierChoice -eq "2") { $DistributionTier = "commercial" } else { $DistributionTier = "foss" }
}
$env:BUILD_DISTRIBUTION_TIER = $DistributionTier

if (-not $NonInteractive) {
    $LicenseIn = Read-Host "Open-source license (MIT/Apache-2.0) [MIT]"
    if ($LicenseIn) { $License = $LicenseIn }
}
if ($License -notin @("MIT", "Apache-2.0")) {
    Write-Host "Invalid license '$License'; defaulting to MIT."
    $License = "MIT"
}

$preArgs = @("--pre", "--stack", $Stack)
if ($SkipPreflight) { $preArgs += "--skip-preflight" }
if ($StrictPreflight) { $preArgs += "--strict" }
bash scripts/bootstrap-lifecycle.sh @preArgs
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

if ($Stack -and $ProjectPurpose) {
    $placeholderPy = @'
import sys
from pathlib import Path

stack, purpose, root = sys.argv[1], sys.argv[2], Path(sys.argv[3])
replacements = [
    ("[INSERT PLATFORM / TECH STACK HERE]", stack),
    ("[INSERT DETAILED APP DESCRIPTION AND GOALS HERE]", purpose),
]
for rel in ("docs/INITIALIZATION_PROMPT.md", "AGENT_MEMORY.md"):
    path = root / rel
    if not path.is_file():
        continue
    text = path.read_text(encoding="utf-8")
    for old, new in replacements:
        text = text.replace(old, new)
    path.write_text(text, encoding="utf-8")
'@
    $placeholderPy | python3 - $Stack $ProjectPurpose $Root
}

$config = Get-Content (Join-Path $Root ".template-update.json") -Raw | ConvertFrom-Json
$config.check_interval = $Interval
Write-Utf8NoBom (Join-Path $Root ".template-update.json") ($config | ConvertTo-Json -Depth 5)


if (-not $ReleaseRepo -and -not $NonInteractive) {
    $ReleaseRepo = Read-Host "GitHub owner/repo for app release checks (OWNER/REPO) [skip]"
}
if (-not $DonationUrl -and -not $NonInteractive) {
    $DonationUrl = Read-Host "Donation URL [skip]"
}
if (-not $Topics -and -not $NonInteractive) {
    $Topics = Read-Host "GitHub topics (comma-separated, 3-5) [skip]"
}
$AppExample = Join-Path $Root ".app-update.json.example"
$AppConfig = Join-Path $Root ".app-update.json"
if ((Test-Path $AppExample) -and -not (Test-Path $AppConfig)) { Copy-Item $AppExample $AppConfig }
if ($ReleaseRepo -and (Test-Path $AppConfig)) {
  $app = Get-Content $AppConfig -Raw | ConvertFrom-Json
  $app.release_repo = $ReleaseRepo.Trim()
  Write-Utf8NoBom $AppConfig ($app | ConvertTo-Json -Depth 5)
}
$DonExample = Join-Path $Root "donations.json.example"
$DonConfig = Join-Path $Root "donations.json"
if ((Test-Path $DonExample) -and -not (Test-Path $DonConfig)) { Copy-Item $DonExample $DonConfig }
if ($DonationUrl -and (Test-Path $DonConfig)) {
  $don = Get-Content $DonConfig -Raw | ConvertFrom-Json
  $don.links = @(@{ label = "Donate"; url = $DonationUrl.Trim() })
  Write-Utf8NoBom $DonConfig ($don | ConvertTo-Json -Depth 5)
}

python3 scripts/sync-stack-config.py $Root $ReleaseRepo $DonationUrl
$env:PYTHONPATH = (Join-Path $Root "scripts/lib")
python3 - $Root $DonationUrl @'
import sys
from pathlib import Path
from init_extras import write_funding_yml
path = write_funding_yml(Path(sys.argv[1]), sys.argv[2])
if path:
    print(f"Wrote {path} (GitHub shows a Sponsor button from this file)")
'@

if (-not $CodeOwner -and -not $NonInteractive) {
    $CodeOwner = Read-Host "GitHub username for CODEOWNERS (without @)"
}
if ($CodeOwner) {
    $codeownersPath = Join-Path $Root ".github/CODEOWNERS"
    if (Test-Path $codeownersPath) {
        $co = Get-Content $codeownersPath -Raw
        $co = $co -replace '@\[PROJECT_OWNER\]', "@$CodeOwner"
        [System.IO.File]::WriteAllText($codeownersPath, $co, (New-Object System.Text.UTF8Encoding $false))
    }
}

$About = "$ProjectName - $ProjectPurpose. Built with agent-project-bootstrap. FOSS MIT."
$aboutContent = @"
# GitHub About Block

## Draft Description (edit to <=350 chars)

$About

## Topics

Add topics relevant to your project and stack.

Suggested for GitHub discoverability (Settings → About).
"@
[System.IO.File]::WriteAllText((Join-Path $Root "docs/GITHUB_ABOUT.md"), $aboutContent, (New-Object System.Text.UTF8Encoding $false))
if ($Topics) {
    python3 - $Root $Topics @'
import sys
from pathlib import Path
from init_extras import gh_topics_command, write_topics
root = Path(sys.argv[1])
topics = [t.strip() for t in sys.argv[2].split(",") if t.strip()]
path = write_topics(root, topics)
cmd = gh_topics_command(topics)
if path:
    print(f"Wrote topics into {path}")
if cmd:
    print(f"Human: apply topics with: {cmd}")
'@
}

$Pruned = $false
if ($Stack -eq "none") {
    Write-Host "Stack 'none': keeping all examples and modules."
} elseif ($Stack -eq "multi") {
    if ($Prune) {
        Write-Host "Keeping all examples (multi-stack)."
    } elseif ($NoPrune -or $NonInteractive) {
        Write-Host "Skipping prune (-NoPrune or -NonInteractive)."
    } else {
        $PruneAnswer = Read-Host "Prune unused examples/modules? (y/N)"
        if ($PruneAnswer -eq "y" -or $PruneAnswer -eq "Y") {
            Write-Host "Keeping all examples (multi-stack)."
        }
    }
} else {
    if ($Prune) {
        $Pruned = $true
        Remove-PrimaryStack $Stack
    } elseif ($NoPrune -or $NonInteractive) {
        Write-Host "Skipping prune (-NoPrune or -NonInteractive)."
    } else {
        $PruneAnswer = Read-Host "Prune unused examples/modules? (y/N)"
        if ($PruneAnswer -eq "y" -or $PruneAnswer -eq "Y") {
            $Pruned = $true
            Remove-PrimaryStack $Stack
        }
    }
}

python3 scripts/init-stack-sync.py $Stack $Root ($Pruned.ToString().ToLower())
$CopyComm = @()
if ($DistributionTier -eq "commercial") { $CopyComm = @("--copy-commercial") }
python3 scripts/sync-cursor-features.py --root $Root --tier $DistributionTier --patch-init @CopyComm
python3 scripts/sync-design-tokens.py 2>$null
python3 scripts/generate-project-readme.py 2>$null
bash scripts/bootstrap-lifecycle.sh --post `
  --stack $Stack `
  --project-name $ProjectName `
  --purpose $ProjectPurpose `
  --license $License `
  --distribution-tier $DistributionTier
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host "Wrote .cursor/stack-selection.json (tier=$DistributionTier) and synced AGENT_MEMORY active modules."

Write-Host ""
Write-Host "=== Workflow validation ===" -ForegroundColor Cyan
if (Get-Command gh -ErrorAction SilentlyContinue) {
    bash scripts/validate-workflow-actions.sh
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Workflow action refs validated via GitHub API."
    } else {
        Write-Host "WARN: validate-workflow-actions.sh failed. Fix refs before first push."
    }
} else {
    Write-Host "WARN: gh CLI not found. Install GitHub CLI and run:"
    Write-Host "  bash scripts/validate-workflow-actions.sh"
    Write-Host "See README.md and docs/SECURITY_TRIAGE.md for setup."
}

Write-Host ""
Write-Host "=== Done ===" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:"
Write-Host "  1. Review SECURITY.md, CODEOWNERS, playbooks, and .env.example"
Write-Host "  2. Run scripts/setup-github-repo.sh (or .ps1) for Dependabot alerts, private reporting, branch protection"
Write-Host "     See docs/SECURITY_TRIAGE.md if the script prints a manual checklist (API 422)"
Write-Host "  3. Open Cursor and paste:"
Write-Host ""
Write-Host "  Read @docs/START_HERE.md, @docs/CURSOR_MODES.md, and @docs/INITIALIZATION_PROMPT.md."
Write-Host "  Pick Cursor mode per CURSOR_MODES.md. Follow Section 8 Startup Sequence."
Write-Host "  Use BUILD_PLAN.md Sequential lane first; respect AGENT/HUMAN/ADB/AUTO labels."
Write-Host ""
Write-Host "  4. After first push to main, poll required workflows:"
Write-Host "     pwsh scripts/check-github-ci.ps1 -WaitSeconds 300"
Write-Host ""
Write-Host "  5. Install pre-commit hooks and preview ephemeral purge:"
Write-Host "     pip install pre-commit; pre-commit install"
Write-Host "     bash scripts/purge-ephemeral.sh"
Write-Host ""
Write-Host "What was set up and why:"
Write-Host "  - Preflight checked git/Python so init fails fast instead of halfway."
Write-Host "  - AGENTS.md adapters + PROJECT_CHECKLIST.md so agents and humans share one Definition of Done."
Write-Host "  - Security defaults (SECURITY.md, Dependabot, CI) are on so the first PR is already gated."
Write-Host "  - Read docs/BEST_PRACTICES.md and docs/FIRST_30_DAYS.md; type /coach for the next action."
Write-Host ""
Write-Host "GitHub About draft: docs/GITHUB_ABOUT.md"
Write-Host "Stack selection: .cursor/stack-selection.json"
Write-Host "Manifest: bootstrap.config.json"
Write-Host "Definition of Done: PROJECT_CHECKLIST.md"
Write-Host "Agent shortcuts: docs/help/BATCH_COMMANDS.md (type / in Agent chat)"
